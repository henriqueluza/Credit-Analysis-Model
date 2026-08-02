package com.creditanalysis.creditapi.infrastructure.client;

import java.math.BigDecimal;

/** Corpo de POST /predict no microsserviço de ML — ver docs/architecture.md. */
public record SolicitacaoAnaliseRequest(
        int idade,
        BigDecimal salarioAnual,
        String situacaoMoradia,
        BigDecimal saldoContaCorrente,
        BigDecimal saldoContaPoupanca,
        BigDecimal valorEmprestimo,
        int prazoMeses
) {
}
