from contextlib import asynccontextmanager
from pathlib import Path
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field, field_validator
from typing import Literal
import pandas as pd
import numpy as np
import joblib
from sqlalchemy import create_engine, Column, Integer, String, Float, DateTime, DECIMAL
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from datetime import datetime
import os
from dotenv import load_dotenv

load_dotenv()

SituacaoMoradia = Literal['own', 'rent', 'free']



DATABASE_URL = os.getenv(
    "DATABASE_URL"
)

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


class ClienteInput(BaseModel):
    idade: int = Field(title="Idade", ge=18, description="Idade do cliente deve ser maior que 18")
    valor_conta_poupanca: float = Field(title="Valor presente na conta poupança",
                                        description='Valor presente na conta poupança do cliente', ge=0)
    valor_conta_corrente: float = Field(title="Valor presente na conta corrente",
                                        description='Valor presente na conta corrente do cliente', ge=0)
    salario_anual: float = Field(title='Salário Anual', description="Salário anual do cliente", ge=0)
    valor_emprestimo: float = Field(title="Valor do empréstimo", description="Valor do empréstimo", gt=0)
    prazo_meses: int = Field(title="Prazo do empréstimo", description="Prazo em meses do empréstimo", gt=0)
    situacao_moradia: SituacaoMoradia = Field(title="Situação da moradia",
                                              description="Cliente é dono, mora de aluguel ou mora de graça?")

    @field_validator('idade')
    @classmethod
    def validar_idade(cls, v):
        if v > 120:
            raise ValueError('Idade inválida; Deve ser menor que 120 anos')
        return v

    @field_validator('prazo_meses')
    @classmethod
    def validar_prazo(cls, v):
        if v > 360:
            raise ValueError("Prazo inválido; Prazo deve ser no máximo 30 anos")
        return v


class Prediction(Base):
    __tablename__ = "predictions"
    id = Column(Integer, primary_key=True, index=True)
    timestamp = Column(DateTime, default=datetime.now)
    idade = Column(Integer)
    valor_conta_poupanca = Column(DECIMAL(10, 2))
    valor_conta_corrente = Column(DECIMAL(10, 2))
    salario_anual = Column(DECIMAL(10, 2))
    valor_emprestimo = Column(DECIMAL(10, 2))
    prazo_meses = Column(Integer)
    situacao_moradia = Column(String(10))
    resultado = Column(String(20))
    probabilidade_risco = Column(DECIMAL(5, 4))
    threshold_utilizado = Column(DECIMAL(5, 4))


def preparar_dados_modelo(cliente: ClienteInput) -> pd.DataFrame:
    # cria as variáveis derivadas
    parcela_mensal_estimada = cliente.valor_emprestimo / cliente.prazo_meses
    renda_mensal = cliente.salario_anual / 12
    renda_livre_mensal = renda_mensal - parcela_mensal_estimada
    comprometimento_renda = parcela_mensal_estimada / max(renda_mensal, 1)
    cobertura_liquidez = (cliente.valor_conta_corrente + cliente.valor_conta_poupanca) / (
                parcela_mensal_estimada + 0.01)
    is_conta_corrente_zero = 1 if cliente.valor_conta_corrente == 0 else 0

    # cria as variáveis logaritmicas
    log_valor_emprestimo = np.log1p(cliente.valor_emprestimo)
    log_valor_conta_corrente = np.log1p(cliente.valor_conta_corrente)

    # one hot encoding colocando free como referência
    moradia_own = 1 if cliente.situacao_moradia == "own" else 0
    moradia_rent = 1 if cliente.situacao_moradia == "rent" else 0

    dados = {
        'idade': [cliente.idade],
        'log_valor_emprestimo': [log_valor_emprestimo],
        'log_valor_conta_corrente': [log_valor_conta_corrente],
        'is_conta_corrente_zero': [is_conta_corrente_zero],
        'prazo_meses': [cliente.prazo_meses],
        'comprometimento_renda': [comprometimento_renda],
        'parcela_mensal_estimada': [parcela_mensal_estimada],
        'renda_livre_mensal': [renda_livre_mensal],
        'cobertura_liquidez': [cobertura_liquidez],
        'moradia_own': [moradia_own],
        'moradia_rent': [moradia_rent]
    }

    return pd.DataFrame(dados)


modelos = {}

CAMINHO_MODELO = Path(__file__).parent.parent.parent / "modelos" / "modelo_credito_final.joblib"


@asynccontextmanager
async def lifespan(app: FastAPI):
    try:
        dados_modelo = joblib.load(CAMINHO_MODELO)
        modelos['pipeline'] = dados_modelo['modelo']
        modelos['threshold'] = dados_modelo['threshold_f2']
        modelos['features'] = dados_modelo['features']
        print("✅ Modelo carregado com sucesso!")
    except FileNotFoundError:
        raise

    # Criar tabelas no banco de dados
    try:
        Base.metadata.create_all(bind=engine)
        print("✅ Tabelas do banco de dados criadas/verificadas com sucesso!")
    except Exception as e:
        print(f"⚠️ Aviso: Não foi possível conectar ao banco de dados: {e}")
        print("   A API continuará funcionando, mas sem salvar predições.")

    yield

    modelos.clear()


app = FastAPI(title="Sistema de Análise de Crédito", lifespan=lifespan)


@app.post('/predict')
def predict_credit(cliente: ClienteInput):
    if 'pipeline' not in modelos or 'features' not in modelos:
        raise HTTPException(status_code=503, detail="O modelo ainda não foi carregado. Tente novamente em segundos.")

    try:
        df = preparar_dados_modelo(cliente)
        colunas_esperadas = modelos['features']
        colunas_faltantes = set(colunas_esperadas) - set(df.columns)
        if colunas_faltantes:
            raise HTTPException(status_code=500, detail=f"Erro interno: Faltam colunas calculadas: {colunas_faltantes}")

        df_final = df[colunas_esperadas]

        pipeline = modelos['pipeline']
        proba = pipeline.predict_proba(df_final)[0][1]
        threshold = modelos['threshold']

        # Inverte probabilidade (proba = prob de ser BOM, queremos prob de risco)
        proba_inadimplente = 1 - proba
        aprovado = proba_inadimplente < threshold

        resultado_dict = {
            'resultado': "Aprovado" if aprovado else "Reprovado",
            'probabilidade_risco': round(float(proba_inadimplente), 4),
            'threshold_utilizado': round(float(threshold), 4)
        }


        try:
            db = SessionLocal()
            nova_predicao = Prediction(
                idade=cliente.idade,
                valor_conta_poupanca=cliente.valor_conta_poupanca,
                valor_conta_corrente=cliente.valor_conta_corrente,
                salario_anual=cliente.salario_anual,
                valor_emprestimo=cliente.valor_emprestimo,
                prazo_meses=cliente.prazo_meses,
                situacao_moradia=cliente.situacao_moradia,
                resultado=resultado_dict['resultado'],
                probabilidade_risco=resultado_dict['probabilidade_risco'],
                threshold_utilizado=resultado_dict['threshold_utilizado']
            )
            db.add(nova_predicao)
            db.commit()
            db.refresh(nova_predicao)
            resultado_dict['prediction_id'] = nova_predicao.id
            db.close()
            print(f"✅ Predição ID {nova_predicao.id} salva no banco de dados")
        except Exception as e:
            print(f"⚠️ Erro ao salvar no banco: {e}")
            # Continua mesmo se falhar (não quebra a API)

        return resultado_dict

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get('/predictions')
def get_predictions(limit: int = 10, skip: int = 0):

    try:
        if limit > 100:
            limit = 100

        db = SessionLocal()
        predictions = (
            db.query(Prediction)
            .order_by(Prediction.timestamp.desc())
            .offset(skip)
            .limit(limit)
            .all()
        )

        total = db.query(Prediction).count()
        db.close()

        return {
            'total': total,
            'limit': limit,
            'skip': skip,
            'predictions': [{
                'id': p.id,
                'timestamp': p.timestamp.isoformat(),
                'idade': p.idade,
                'valor_conta_poupanca': float(p.valor_conta_poupanca),
                'valor_conta_corrente': float(p.valor_conta_corrente),
                'salario_anual': float(p.salario_anual),
                'valor_emprestimo': float(p.valor_emprestimo),
                'prazo_meses': p.prazo_meses,
                'situacao_moradia': p.situacao_moradia,
                'resultado': p.resultado,
                'probabilidade_risco': float(p.probabilidade_risco),
            } for p in predictions]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro ao buscar predições: {str(e)}")


@app.get('/predictions/stats')
def get_stats():
    """
    Retorna estatísticas básicas das predições
    """
    try:
        db = SessionLocal()

        total = db.query(Prediction).count()
        aprovados = db.query(Prediction).filter(Prediction.resultado == "Aprovado").count()
        reprovados = db.query(Prediction).filter(Prediction.resultado == "Reprovado").count()

        db.close()

        return {
            'total_predicoes': total,
            'aprovados': aprovados,
            'reprovados': reprovados,
            'taxa_aprovacao': round(aprovados / total * 100, 2) if total > 0 else 0
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro ao calcular estatísticas: {str(e)}")


@app.get('/predictions/{prediction_id}')
def get_prediction_by_id(prediction_id: int):
    """
    Retorna uma predição específica por ID
    """
    try:
        db = SessionLocal()
        prediction = db.query(Prediction).filter(Prediction.id == prediction_id).first()
        db.close()

        if not prediction:
            raise HTTPException(status_code=404, detail="Predição não encontrada")

        return {
            'id': prediction.id,
            'timestamp': prediction.timestamp.isoformat(),
            'idade': prediction.idade,
            'valor_conta_poupanca': float(prediction.valor_conta_poupanca),
            'valor_conta_corrente': float(prediction.valor_conta_corrente),
            'salario_anual': float(prediction.salario_anual),
            'valor_emprestimo': float(prediction.valor_emprestimo),
            'prazo_meses': prediction.prazo_meses,
            'situacao_moradia': prediction.situacao_moradia,
            'resultado': prediction.resultado,
            'probabilidade_risco': float(prediction.probabilidade_risco),
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro ao buscar predição: {str(e)}")