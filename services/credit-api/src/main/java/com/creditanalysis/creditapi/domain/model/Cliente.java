package com.creditanalysis.creditapi.domain.model;

import com.creditanalysis.creditapi.domain.exception.SolicitacaoInvalidaException;

import java.math.BigDecimal;

public record Cliente(
        int idade,
        BigDecimal salarioAnual,
        SituacaoMoradia situacaoMoradia,
        BigDecimal saldoContaCorrente,
        BigDecimal saldoContaPoupanca
) {
    public Cliente {
        if (idade < 18 || idade > 120) {
            throw new SolicitacaoInvalidaException("Idade deve estar entre 18 e 120 anos");
        }
        if (salarioAnual.signum() < 0) {
            throw new SolicitacaoInvalidaException("Salário anual não pode ser negativo");
        }
        if (saldoContaCorrente.signum() < 0) {
            throw new SolicitacaoInvalidaException("Saldo em conta corrente não pode ser negativo");
        }
        if (saldoContaPoupanca.signum() < 0) {
            throw new SolicitacaoInvalidaException("Saldo em conta poupança não pode ser negativo");
        }
    }
}
