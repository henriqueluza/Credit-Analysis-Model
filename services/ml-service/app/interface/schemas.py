from typing import Any

from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel

from app.domain.entities import SituacaoMoradia, StatusAnalise


class SolicitacaoAnaliseRequest(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    idade: int = Field(ge=18, le=120)
    salario_anual: float = Field(ge=0)
    situacao_moradia: SituacaoMoradia
    saldo_conta_corrente: float = Field(ge=0)
    saldo_conta_poupanca: float = Field(ge=0)
    valor_emprestimo: float = Field(gt=0)
    prazo_meses: int = Field(gt=0, le=360)


class ResultadoPredicaoResponse(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    resultado: StatusAnalise
    probabilidade_risco: float
    threshold_utilizado: float
    versao_modelo: str


class ModelInfoResponse(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    versao_modelo: str
    data_treino: str
    metricas: dict[str, Any]
    features: list[str]
