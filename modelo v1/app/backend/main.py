from contextlib import asynccontextmanager
from pathlib import Path
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field, field_validator
from typing import Literal
import pandas as pd
import numpy as np
import joblib

SituacaoMoradia = Literal['own', 'rent', 'free']

class ClienteInput(BaseModel):
    idade: int = Field(title="Idade", ge=18, description="Idade do cliente deve ser maior que 18")
    valor_conta_poupanca: float = Field(title="Valor presente na conta poupança", description='Valor presente na conta poupança do cliente', ge=0)
    valor_conta_corrente: float = Field(title="Valor presente na conta corrente", description='Valor presente na conta corrente do cliente', ge=0)
    salario_anual: float = Field(title='Salário Anual', description="Salário anual do cliente",ge=0)
    valor_emprestimo: float = Field(title="Valor do empréstimo", description="Valor do empréstimo", gt=0)
    prazo_meses: int = Field(title="Prazo do empréstimo", description="Prazo em meses do empréstimo", gt=0)
    situacao_moradia: SituacaoMoradia = Field(title="Situação da moradia", description="Cliente é dono, mora de aluguel ou mora de graça?")

    # função decoradora para validar se a idade é menor que 120 anos
    @field_validator('idade')
    @classmethod
    def validar_idade(cls, v):
        if v > 120:
            raise ValueError('Idade inválida; Deve ser menor que 120 anos')
        return v

    # função decoradora para validar se o prazo é menor que 30 anos (360 meses)
    @field_validator('prazo_meses')
    @classmethod
    def validar_prazo(cls, v):
        if v > 360:
            raise ValueError("Prazo inválido; Prazo deve ser no máximo 30 anos")
        return v


def preparar_dados_modelo(cliente: ClienteInput) -> pd.DataFrame:

    # a função usa typehints para dizer que o input cliente deve ser do tipo ClienteInput e retorna um df do pandas

    # cria as variáveis derivadas

    parcela_mensal_estimada = cliente.valor_emprestimo / cliente.prazo_meses
    renda_mensal = cliente.salario_anual / 12
    renda_livre_mensal = renda_mensal - parcela_mensal_estimada
    comprometimento_renda = parcela_mensal_estimada / max(renda_mensal, 1)
    cobertura_liquidez = (cliente.valor_conta_corrente + cliente.valor_conta_poupanca) / (parcela_mensal_estimada + 0.01)
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

    # cria dicionário para converter em dataframe

    return pd.DataFrame(dados)

modelos = {}

# Caminho absoluto para o modelo
CAMINHO_MODELO = Path(__file__).parent.parent.parent / "modelos" / "modelo_credito_final.joblib"

@asynccontextmanager
async def lifespan(app: FastAPI):
    try:
        dados_modelo = joblib.load(CAMINHO_MODELO)
        modelos['pipeline'] = dados_modelo['modelo']
        modelos['threshold'] = dados_modelo['threshold_f2']
        modelos['features'] = dados_modelo['features']
    except FileNotFoundError:
        raise

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
        aprovado = proba < threshold

        return {
            'resultado': "Aprovado" if aprovado else "Reprovado",
            'probabilidade_risco': round(float(proba), 4),
            'threshold_utilizado': round(float(threshold), 4)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))