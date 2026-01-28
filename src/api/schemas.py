from pydantic import BaseModel, Field

class ClientInput(BaseModel):
    renda_mensal: float = Field(ge=0, description="Quanto o cliente ganha por mês")
    tem_atraso_grave: bool = Field(description="Teve algum atraso grave (Maior que 90 dias)?")
    total_atrasos: int = Field(ge=0, description="Quantas vezes atrasou o pagamento do cartão de crédito?")
    idade: int = Field(ge=18, description='Idade') # maior que 18
    limite: float = Field(gt=0, description='Qual o limite do cartão?')
    uso_limite: float = Field(ge=0, description="Quanto usou do limite?")
    divida: float = Field(ge=0, description="Quanto tem de dívida?") # para calcular a razao_divida_renda
    num_dependentes: int = Field(ge=0, description="Quantos dependentes financeiros?") # positivo
    linhas_credito_abertas: int = Field(ge=0, description="Quantas linhas de crédito abertas?")
    emprestimos_imobiliarios: int = Field(ge=0, description="Quantos empréstimos imobiliários o cliente possui?")

class PredictionResponse(BaseModel):
    client_id: int
    prediction: int
    resultado: str
    probability: float
    model_version: str