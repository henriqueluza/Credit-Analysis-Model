package com.creditanalysis.creditapi.application;

import com.creditanalysis.creditapi.domain.model.Cliente;
import com.creditanalysis.creditapi.domain.model.ResultadoAnalise;
import com.creditanalysis.creditapi.domain.model.SolicitacaoEmprestimo;
import com.creditanalysis.creditapi.domain.port.ModeloCreditoClient;
import com.creditanalysis.creditapi.domain.port.PredicaoRisco;
import com.creditanalysis.creditapi.domain.repository.SolicitacaoEmprestimoRepository;

import java.time.Instant;

public class AnalisarCreditoUseCase {

    private final ModeloCreditoClient modeloCreditoClient;
    private final SolicitacaoEmprestimoRepository repository;

    public AnalisarCreditoUseCase(ModeloCreditoClient modeloCreditoClient, SolicitacaoEmprestimoRepository repository) {
        this.modeloCreditoClient = modeloCreditoClient;
        this.repository = repository;
    }

    public SolicitacaoEmprestimo executar(ComandoAnalisarCredito comando) {
        Cliente cliente = new Cliente(
                comando.idade(),
                comando.salarioAnual(),
                comando.situacaoMoradia(),
                comando.saldoContaCorrente(),
                comando.saldoContaPoupanca()
        );

        SolicitacaoEmprestimo solicitacao = SolicitacaoEmprestimo.nova(
                cliente, comando.valorEmprestimo(), comando.prazoMeses(), comando.usuarioId()
        );

        PredicaoRisco predicao = modeloCreditoClient.avaliar(cliente, comando.valorEmprestimo(), comando.prazoMeses());

        ResultadoAnalise resultado = new ResultadoAnalise(
                predicao.resultado(),
                predicao.probabilidadeRisco(),
                predicao.thresholdUtilizado(),
                predicao.versaoModelo(),
                Instant.now()
        );

        return repository.salvar(solicitacao.comResultado(resultado));
    }
}
