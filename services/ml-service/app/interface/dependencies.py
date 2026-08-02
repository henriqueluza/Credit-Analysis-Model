from fastapi import Request

from app.application.predizer_risco_use_case import PredizerRiscoUseCase
from app.domain.ports import ModeloPreditivo


def get_modelo_preditivo(request: Request) -> ModeloPreditivo:
    return request.app.state.modelo_preditivo


def get_predizer_risco_use_case(request: Request) -> PredizerRiscoUseCase:
    return PredizerRiscoUseCase(get_modelo_preditivo(request))
