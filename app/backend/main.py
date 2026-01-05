from dataclasses import field

from fastapi import FastAPI
from pydantic import BaseModel, Field
from typing import Literal

app = FastAPI(title="Sistema de Análise de Crédito")

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

@app.get("/")
def read_root():
    return {"Hello": "World"}