import json
from pathlib import Path

import joblib
import pandas as pd

from app.domain.ports import InformacoesModelo, ModeloPreditivo


class ModeloJoblibRepository(ModeloPreditivo):
    """Implementa a porta ModeloPreditivo carregando o pipeline treinado (.joblib)
    e os metadados de versionamento salvos junto ao artefato.
    """

    def __init__(self, caminho_modelo: Path, caminho_metadata: Path) -> None:
        dados_modelo = joblib.load(caminho_modelo)
        self._pipeline = dados_modelo["modelo"]
        self._threshold = float(dados_modelo["threshold_f2"])
        self._features_esperadas: list[str] = dados_modelo["features"]

        metadata = json.loads(caminho_metadata.read_text(encoding="utf-8"))
        self._versao = metadata["versao"]
        self._data_treino = metadata["dataTreino"]
        self._metricas = metadata["metricas"]

    @property
    def threshold(self) -> float:
        return self._threshold

    @property
    def versao(self) -> str:
        return self._versao

    def prever_probabilidade_bom_pagador(self, features_derivadas: dict[str, float]) -> float:
        linha = pd.DataFrame([features_derivadas])[self._features_esperadas]
        return float(self._pipeline.predict_proba(linha)[0][1])

    def informacoes(self) -> InformacoesModelo:
        return InformacoesModelo(
            versao=self._versao,
            data_treino=self._data_treino,
            metricas=self._metricas,
            features=list(self._features_esperadas),
        )
