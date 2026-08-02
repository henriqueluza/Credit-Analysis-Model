from app.domain.entities import StatusAnalise


def decidir_resultado(probabilidade_risco: float, threshold: float) -> StatusAnalise:
    """Aprovado quando a probabilidade de risco fica abaixo do threshold do modelo."""
    return StatusAnalise.APROVADO if probabilidade_risco < threshold else StatusAnalise.REPROVADO
