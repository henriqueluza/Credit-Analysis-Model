from dataclasses import dataclass
from enum import Enum

from app.domain.exceptions import FeaturesInvalidasError


class SituacaoMoradia(str, Enum):
    OWN = "OWN"
    RENT = "RENT"
    FREE = "FREE"


class StatusAnalise(str, Enum):
    APROVADO = "APROVADO"
    REPROVADO = "REPROVADO"


@dataclass(frozen=True)
class FeaturesEmprestimo:
    """Dados de entrada validados do cliente e do pedido de empréstimo.

    Corresponde ao que o Spring Boot envia em POST /predict: dados já
    tipados e validados, mas ainda não transformados nas features derivadas
    que o modelo espera (isso é responsabilidade de feature_engineering.py).
    """

    idade: int
    salario_anual: float
    situacao_moradia: SituacaoMoradia
    saldo_conta_corrente: float
    saldo_conta_poupanca: float
    valor_emprestimo: float
    prazo_meses: int

    def __post_init__(self) -> None:
        if self.idade < 18 or self.idade > 120:
            raise FeaturesInvalidasError("Idade deve estar entre 18 e 120 anos")
        if self.salario_anual < 0:
            raise FeaturesInvalidasError("Salário anual não pode ser negativo")
        if self.saldo_conta_corrente < 0:
            raise FeaturesInvalidasError("Saldo em conta corrente não pode ser negativo")
        if self.saldo_conta_poupanca < 0:
            raise FeaturesInvalidasError("Saldo em conta poupança não pode ser negativo")
        if self.valor_emprestimo <= 0:
            raise FeaturesInvalidasError("Valor do empréstimo deve ser maior que zero")
        if self.prazo_meses <= 0 or self.prazo_meses > 360:
            raise FeaturesInvalidasError("Prazo deve estar entre 1 e 360 meses")


@dataclass(frozen=True)
class ResultadoPredicao:
    resultado: StatusAnalise
    probabilidade_risco: float
    threshold_utilizado: float
    versao_modelo: str
