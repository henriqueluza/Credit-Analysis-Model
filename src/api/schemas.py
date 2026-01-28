from pydantic import BaseModel, Field

class ClientInput(BaseModel):
    renda_mensal: float = Field(ge=0)
    tem_atraso_grave: bool
    total_atrasos: int = Field(ge=0)
    idade: int = Field(ge=18) # maior que 18
    limite: float = Field(gt=0)
    uso_limite: float = Field(ge=0)
    divida: float = Field(ge=0)# para calcular a razao_divida_renda
    num_dependentes: int = Field(ge=0) # positivo
    linhas_credito_abertas: int = Field(ge=0)
    emprestimos_imobiliarios: int = Field(ge=0)

class PredictionResponse(BaseModel):
    client_id: int
    prediction: int
    resultado: str
    probability: float
    model_version: str