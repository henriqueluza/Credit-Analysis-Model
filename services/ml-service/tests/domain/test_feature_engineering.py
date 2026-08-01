import pytest

from app.domain.entities import FeaturesEmprestimo, SituacaoMoradia
from app.domain.feature_engineering import calcular_features_derivadas


def _features(**overrides) -> FeaturesEmprestimo:
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
    return FeaturesEmprestimo(**base)


def test_calcula_features_derivadas_com_valores_esperados():
    resultado = calcular_features_derivadas(_features())

    assert resultado["idade"] == 35
    assert resultado["prazo_meses"] == 24
    assert resultado["is_conta_corrente_zero"] == 0
    assert resultado["moradia_own"] == 1
    assert resultado["moradia_rent"] == 0
    assert resultado["parcela_mensal_estimada"] == pytest.approx(416.6666666666667)
    assert resultado["renda_livre_mensal"] == pytest.approx(4583.333333333333)
    assert resultado["comprometimento_renda"] == pytest.approx(0.08333333333333334)
    assert resultado["cobertura_liquidez"] == pytest.approx(15.599625608985384)
    assert resultado["log_valor_emprestimo"] == pytest.approx(9.210440366976517)
    assert resultado["log_valor_conta_corrente"] == pytest.approx(7.313886831633462)


def test_marca_conta_corrente_zerada():
    resultado = calcular_features_derivadas(_features(saldo_conta_corrente=0.0))

    assert resultado["is_conta_corrente_zero"] == 1
    assert resultado["log_valor_conta_corrente"] == 0.0


def test_one_hot_moradia_rent():
    resultado = calcular_features_derivadas(_features(situacao_moradia=SituacaoMoradia.RENT))

    assert resultado["moradia_own"] == 0
    assert resultado["moradia_rent"] == 1


def test_one_hot_moradia_free_e_referencia_sem_flags():
    resultado = calcular_features_derivadas(_features(situacao_moradia=SituacaoMoradia.FREE))

    assert resultado["moradia_own"] == 0
    assert resultado["moradia_rent"] == 0


def test_comprometimento_renda_nao_divide_por_zero_quando_sem_salario():
    resultado = calcular_features_derivadas(_features(salario_anual=0.0))

    assert resultado["comprometimento_renda"] == pytest.approx(416.6666666666667)
