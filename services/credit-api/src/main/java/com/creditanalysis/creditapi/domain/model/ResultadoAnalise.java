package com.creditanalysis.creditapi.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record ResultadoAnalise(
        StatusAnalise resultado,
        BigDecimal probabilidadeRisco,
        BigDecimal thresholdUtilizado,
        String versaoModelo,
        Instant analisadoEm
) {
}
