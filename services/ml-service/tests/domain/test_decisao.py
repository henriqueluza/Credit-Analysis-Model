from app.domain.decisao import decidir_resultado
from app.domain.entities import StatusAnalise


def test_aprova_quando_probabilidade_abaixo_do_threshold():
    assert decidir_resultado(probabilidade_risco=0.1, threshold=0.35) == StatusAnalise.APROVADO


def test_reprova_quando_probabilidade_acima_do_threshold():
    assert decidir_resultado(probabilidade_risco=0.6, threshold=0.35) == StatusAnalise.REPROVADO


def test_reprova_quando_probabilidade_igual_ao_threshold():
    assert decidir_resultado(probabilidade_risco=0.35, threshold=0.35) == StatusAnalise.REPROVADO
