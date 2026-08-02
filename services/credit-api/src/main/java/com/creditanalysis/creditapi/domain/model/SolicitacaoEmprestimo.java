package com.creditanalysis.creditapi.domain.model;

import com.creditanalysis.creditapi.domain.exception.SolicitacaoInvalidaException;

import java.math.BigDecimal;
import java.time.Instant;

public record SolicitacaoEmprestimo(
        Long id,
        Cliente cliente,
        BigDecimal valorEmprestimo,
        int prazoMeses,
        Long solicitadoPor,
        Instant criadoEm,
        ResultadoAnalise resultado
) {
    public SolicitacaoEmprestimo {
        if (valorEmprestimo == null || valorEmprestimo.signum() <= 0) {
            throw new SolicitacaoInvalidaException("Valor do empréstimo deve ser maior que zero");
        }
        if (prazoMeses <= 0 || prazoMeses > 360) {
            throw new SolicitacaoInvalidaException("Prazo deve estar entre 1 e 360 meses");
        }
    }

    public static SolicitacaoEmprestimo nova(Cliente cliente, BigDecimal valorEmprestimo, int prazoMeses, Long solicitadoPor) {
        return new SolicitacaoEmprestimo(null, cliente, valorEmprestimo, prazoMeses, solicitadoPor, Instant.now(), null);
    }

    public SolicitacaoEmprestimo comResultado(ResultadoAnalise resultado) {
        return new SolicitacaoEmprestimo(id, cliente, valorEmprestimo, prazoMeses, solicitadoPor, criadoEm, resultado);
    }
}
