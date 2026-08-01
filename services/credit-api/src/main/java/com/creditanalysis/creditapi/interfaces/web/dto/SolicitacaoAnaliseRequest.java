package com.creditanalysis.creditapi.interfaces.web.dto;

import com.creditanalysis.creditapi.domain.model.SituacaoMoradia;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record SolicitacaoAnaliseRequest(
        @Min(18) @Max(120) int idade,
        @NotNull @PositiveOrZero BigDecimal salarioAnual,
        @NotNull SituacaoMoradia situacaoMoradia,
        @NotNull @PositiveOrZero BigDecimal saldoContaCorrente,
        @NotNull @PositiveOrZero BigDecimal saldoContaPoupanca,
        @NotNull @Positive BigDecimal valorEmprestimo,
        @Min(1) @Max(360) int prazoMeses
) {
}
