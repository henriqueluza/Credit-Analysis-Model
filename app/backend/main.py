from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field, field_validator
from typing import Literal
import pandas as pd
import numpy as np
import joblib
from sympy.polys.factortools import dmp_factor_list

SituacaoMoradia = Literal['own', 'rent', 'free']
FinalidadeEmprestimo = Literal[
    'business',
    'car',
    'domestic_appliances',
    'education',
    'furniture/equipment',
    'radio/TV',
    'repairs',
    'vacations/others'
]
class ClienteInput(BaseModel):
    idade: int = Field(title="Idade", gt=18, description="Idade do cliente deve ser maior que 18")
    sexo: Literal['male', 'female'] = Field(title='Sexo', description="Sexo do cliente")
    valor_conta_poupanca: float = Field(title="Valor presente na conta poupança", description='Valor presente na conta poupança do cliente', ge=0)
    valor_conta_corrente: float = Field(title="Valor presente na conta corrente", description='Valor presente na conta corrente do cliente', ge=0)
    salario_anual: float = Field(title='Salário Anual', description="Salário anual do cliente",gt=0)
    valor_emprestimo: float = Field(title="Valor do empréstimo", description="Valor do empréstimo", gt=0)
    prazo_meses: int = Field(title="Prazo do empréstimo", description="Prazo em meses do empréstimo", gt=0)
    finalidade_emprestimo: FinalidadeEmprestimo = Field(title="Finalidade do empréstimo", description="Finalidade do empréstimo")
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

  # todas as variáveis de finalidade foram removidas já que não foram usadas para treinar o modelo

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

modelos = {
}

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("Carregando modelo na memória")
    try:
        dados_modelo = joblib.load("../../modelos/modelo_credito_final.joblib")
        modelos['pipeline'] = dados_modelo['modelo']
        modelos['threshold'] = dados_modelo['threshold_f2']
        modelos['features'] = dados_modelo['features']
        print('Modelo carregado com sucesso!')
    except FileNotFoundError:
        print("Erro: Arquivo do modelo não foi encontrado.")
        raise
    yield # divide o código em startup (carrega o modelo) e shutdown (limpeza do modelo)

    modelos.clear()
    print("Modelo removido da memória.")

app = FastAPI(title="Sistema de Análise de Crédito")

@app.post('/predict')
def predict_credit(cliente: ClienteInput):
    if 'pipeline' not in modelos or 'features' not in modelos: # verifica se o modelo foi carregado corretamente
        raise HTTPException(status_code=503, detail="O modelo ainda não foi carregado. Tente novamente em segundos.")
    try:
        df = preparar_dados_modelo(cliente)
        colunas_esperadas = modelos['features'] # deixa as colunas na mesma forma salvas na variável features
        colunas_faltantes = set(colunas_esperadas) - set(df.columns)
        if colunas_faltantes:
            raise HTTPException(status_code=500, detail=f"Erro interno: Faltam colunas calculadas: {colunas_faltantes}")

        df_final = df_input[cols_modelo]

        pipeline = modelos['pipeline']
        proba = pipeline.predict_proba(df_final)[0][1]
        threshold = modelos['threshold']
        aprovado = proba < threshold

        return {
            'resultado': "Aprovado" if aprovado else "Reprovado",
            'probabilidade_risco': round(proba, 4),
            'threshold_utilizado': round(threshold, 4)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
@app.get("/")
def read_root():
    return {"Hello": "World"}