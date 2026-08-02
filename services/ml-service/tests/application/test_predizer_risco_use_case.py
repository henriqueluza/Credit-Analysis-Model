from app.application.predizer_risco_use_case import PredizerRiscoUseCase
from app.domain.entities import FeaturesEmprestimo, SituacaoMoradia, StatusAnalise
from app.domain.ports import InformacoesModelo, ModeloPreditivo


class ModeloPreditivoFake(ModeloPreditivo):
    def __init__(self, probabilidade_bom_pagador: float, threshold: float, versao: str = "1.0.0") -> None:
        self._probabilidade_bom_pagador = probabilidade_bom_pagador
        self._threshold = threshold
        self._versao = versao
        self.features_recebidas: dict[str, float] | None = None

    @property
    def threshold(self) -> float:
        return self._threshold

    @property
    def versao(self) -> str:
        return self._versao

    def prever_probabilidade_bom_pagador(self, features_derivadas: dict[str, float]) -> float:
        self.features_recebidas = features_derivadas
        return self._probabilidade_bom_pagador

    def informacoes(self) -> InformacoesModelo:
        return InformacoesModelo(versao=self._versao, data_treino="2025-12-31", metricas={}, features=[])


def _features() -> FeaturesEmprestimo:
    return FeaturesEmprestimo(
        idade=35,
        salario_anual=60000.0,
        situacao_moradia=SituacaoMoradia.OWN,
        saldo_conta_corrente=1500.0,
        saldo_conta_poupanca=5000.0,
        valor_emprestimo=10000.0,
        prazo_meses=24,
    )


def test_aprova_quando_probabilidade_de_risco_fica_abaixo_do_threshold():
    modelo = ModeloPreditivoFake(probabilidade_bom_pagador=0.9, threshold=0.35)
    use_case = PredizerRiscoUseCase(modelo)

    resultado = use_case.executar(_features())

    assert resultado.resultado == StatusAnalise.APROVADO
    assert resultado.probabilidade_risco == 0.1
    assert resultado.threshold_utilizado == 0.35
    assert resultado.versao_modelo == "1.0.0"


def test_reprova_quando_probabilidade_de_risco_fica_acima_do_threshold():
    modelo = ModeloPreditivoFake(probabilidade_bom_pagador=0.3, threshold=0.35)
    use_case = PredizerRiscoUseCase(modelo)

    resultado = use_case.executar(_features())

    assert resultado.resultado == StatusAnalise.REPROVADO
    assert resultado.probabilidade_risco == 0.7


def test_repassa_features_derivadas_para_o_modelo():
    modelo = ModeloPreditivoFake(probabilidade_bom_pagador=0.9, threshold=0.35)
    use_case = PredizerRiscoUseCase(modelo)

    use_case.executar(_features())

    assert modelo.features_recebidas is not None
    assert modelo.features_recebidas["idade"] == 35
    assert modelo.features_recebidas["moradia_own"] == 1
