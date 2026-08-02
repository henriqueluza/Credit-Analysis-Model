package com.creditanalysis.creditapi.domain.port;

import com.creditanalysis.creditapi.domain.model.StatusAnalise;

import java.math.BigDecimal;

public record PredicaoRisco(
        StatusAnalise resultado,
        BigDecimal probabilidadeRisco,
        BigDecimal thresholdUtilizado,
        String versaoModelo
) {
}
