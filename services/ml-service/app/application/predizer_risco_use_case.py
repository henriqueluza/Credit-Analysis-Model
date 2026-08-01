from app.domain.decisao import decidir_resultado
from app.domain.entities import FeaturesEmprestimo, ResultadoPredicao
from app.domain.feature_engineering import calcular_features_derivadas
from app.domain.ports import ModeloPreditivo


class PredizerRiscoUseCase:
    def __init__(self, modelo: ModeloPreditivo) -> None:
        self._modelo = modelo

    def executar(self, features: FeaturesEmprestimo) -> ResultadoPredicao:
        features_derivadas = calcular_features_derivadas(features)
        probabilidade_bom_pagador = self._modelo.prever_probabilidade_bom_pagador(features_derivadas)
        probabilidade_risco = 1 - probabilidade_bom_pagador
        threshold = self._modelo.threshold

        return ResultadoPredicao(
            resultado=decidir_resultado(probabilidade_risco, threshold),
            probabilidade_risco=round(probabilidade_risco, 4),
            threshold_utilizado=round(threshold, 4),
            versao_modelo=self._modelo.versao,
        )
