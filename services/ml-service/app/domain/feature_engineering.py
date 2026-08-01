import math

from app.domain.entities import FeaturesEmprestimo, SituacaoMoradia


def calcular_features_derivadas(features: FeaturesEmprestimo) -> dict[str, float]:
    """Deriva as features que o modelo foi treinado para consumir.

    Regra de negócio pura, sem dependência de pandas/numpy/frameworks —
    equivalente ao que hoje é feito em preparar_dados_modelo() no monólito.
    """
    parcela_mensal_estimada = features.valor_emprestimo / features.prazo_meses
    renda_mensal = features.salario_anual / 12
    renda_livre_mensal = renda_mensal - parcela_mensal_estimada
    comprometimento_renda = parcela_mensal_estimada / max(renda_mensal, 1)
    cobertura_liquidez = (features.saldo_conta_corrente + features.saldo_conta_poupanca) / (
        parcela_mensal_estimada + 0.01
    )
    is_conta_corrente_zero = 1 if features.saldo_conta_corrente == 0 else 0

    log_valor_emprestimo = math.log1p(features.valor_emprestimo)
    log_valor_conta_corrente = math.log1p(features.saldo_conta_corrente)

    moradia_own = 1 if features.situacao_moradia == SituacaoMoradia.OWN else 0
    moradia_rent = 1 if features.situacao_moradia == SituacaoMoradia.RENT else 0

    return {
        "idade": features.idade,
        "log_valor_emprestimo": log_valor_emprestimo,
        "log_valor_conta_corrente": log_valor_conta_corrente,
        "is_conta_corrente_zero": is_conta_corrente_zero,
        "prazo_meses": features.prazo_meses,
        "comprometimento_renda": comprometimento_renda,
        "parcela_mensal_estimada": parcela_mensal_estimada,
        "renda_livre_mensal": renda_livre_mensal,
        "cobertura_liquidez": cobertura_liquidez,
        "moradia_own": moradia_own,
        "moradia_rent": moradia_rent,
    }
