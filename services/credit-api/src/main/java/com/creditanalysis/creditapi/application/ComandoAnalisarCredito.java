package com.creditanalysis.creditapi.application;

import com.creditanalysis.creditapi.domain.model.SituacaoMoradia;

import java.math.BigDecimal;

public record ComandoAnalisarCredito(
        int idade,
        BigDecimal salarioAnual,
        SituacaoMoradia situacaoMoradia,
        BigDecimal saldoContaCorrente,
        BigDecimal saldoContaPoupanca,
        BigDecimal valorEmprestimo,
        int prazoMeses,
        Long usuarioId
) {
}
