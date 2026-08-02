from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Any


@dataclass(frozen=True)
class InformacoesModelo:
    versao: str
    data_treino: str
    metricas: dict[str, Any]
    features: list[str]


class ModeloPreditivo(ABC):
    """Porta que abstrai 'algo que recebe features derivadas e devolve uma
    probabilidade' — implementada pela infraestrutura (carregando o .joblib).
    """

    @property
    @abstractmethod
    def threshold(self) -> float: ...

    @property
    @abstractmethod
    def versao(self) -> str: ...

    @abstractmethod
    def prever_probabilidade_bom_pagador(self, features_derivadas: dict[str, float]) -> float: ...

    @abstractmethod
    def informacoes(self) -> InformacoesModelo: ...
