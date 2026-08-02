import pytest

from app.domain.entities import FeaturesEmprestimo, SituacaoMoradia
from app.domain.exceptions import FeaturesInvalidasError


def _kwargs(**overrides):
    base = dict(
        idade=35,
        salario_anual=60000.0,
        situacao_moradia=SituacaoMoradia.OWN,
        saldo_conta_corrente=1500.0,
        saldo_conta_poupanca=5000.0,
        valor_emprestimo=10000.0,
        prazo_meses=24,
    )
    base.update(overrides)
    return base


@pytest.mark.parametrize(
    "overrides",
    [
        {"idade": 17},
        {"idade": 121},
        {"salario_anual": -1.0},
        {"saldo_conta_corrente": -1.0},
        {"saldo_conta_poupanca": -1.0},
        {"valor_emprestimo": 0.0},
        {"valor_emprestimo": -100.0},
        {"prazo_meses": 0},
        {"prazo_meses": 361},
    ],
)
def test_rejeita_dados_invalidos(overrides):
    with pytest.raises(FeaturesInvalidasError):
        FeaturesEmprestimo(**_kwargs(**overrides))


def test_aceita_dados_no_limite_valido():
    features = FeaturesEmprestimo(**_kwargs(idade=18, prazo_meses=360))
    assert features.idade == 18
    assert features.prazo_meses == 360
