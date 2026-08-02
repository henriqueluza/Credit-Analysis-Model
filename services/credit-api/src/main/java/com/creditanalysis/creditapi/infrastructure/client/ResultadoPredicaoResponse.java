package com.creditanalysis.creditapi.infrastructure.client;

import java.math.BigDecimal;

/** Resposta de POST /predict no microsserviço de ML — ver docs/architecture.md. */
public record ResultadoPredicaoResponse(
        String resultado,
        BigDecimal probabilidadeRisco,
        BigDecimal thresholdUtilizado,
        String versaoModelo
) {
}
