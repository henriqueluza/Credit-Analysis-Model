package com.creditanalysis.creditapi.interfaces.web.dto;

import com.creditanalysis.creditapi.domain.model.Cliente;
import com.creditanalysis.creditapi.domain.model.ResultadoAnalise;
import com.creditanalysis.creditapi.domain.model.SolicitacaoEmprestimo;

import java.math.BigDecimal;
import java.time.Instant;

public record SolicitacaoAnaliseResponse(
        Long id,
        int idade,
        BigDecimal salarioAnual,
        String situacaoMoradia,
        BigDecimal saldoContaCorrente,
        BigDecimal saldoContaPoupanca,
        BigDecimal valorEmprestimo,
        int prazoMeses,
        Instant criadoEm,
        String resultado,
        BigDecimal probabilidadeRisco,
        BigDecimal thresholdUtilizado,
        String versaoModelo,
        Instant analisadoEm
) {
    public static SolicitacaoAnaliseResponse deDominio(SolicitacaoEmprestimo solicitacao) {
        Cliente cliente = solicitacao.cliente();
        ResultadoAnalise resultado = solicitacao.resultado();
        return new SolicitacaoAnaliseResponse(
                solicitacao.id(),
                cliente.idade(),
                cliente.salarioAnual(),
                cliente.situacaoMoradia().name(),
                cliente.saldoContaCorrente(),
                cliente.saldoContaPoupanca(),
                solicitacao.valorEmprestimo(),
                solicitacao.prazoMeses(),
                solicitacao.criadoEm(),
                resultado.resultado().name(),
                resultado.probabilidadeRisco(),
                resultado.thresholdUtilizado(),
                resultado.versaoModelo(),
                resultado.analisadoEm()
        );
    }
}
