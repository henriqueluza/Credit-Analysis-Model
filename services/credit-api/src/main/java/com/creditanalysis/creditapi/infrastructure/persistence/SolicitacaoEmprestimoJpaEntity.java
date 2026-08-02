package com.creditanalysis.creditapi.infrastructure.persistence;

import com.creditanalysis.creditapi.domain.model.Cliente;
import com.creditanalysis.creditapi.domain.model.ResultadoAnalise;
import com.creditanalysis.creditapi.domain.model.SituacaoMoradia;
import com.creditanalysis.creditapi.domain.model.SolicitacaoEmprestimo;
import com.creditanalysis.creditapi.domain.model.StatusAnalise;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "solicitacoes_emprestimo")
public class SolicitacaoEmprestimoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int idade;

    private BigDecimal salarioAnual;

    @Enumerated(EnumType.STRING)
    private SituacaoMoradia situacaoMoradia;

    private BigDecimal saldoContaCorrente;

    private BigDecimal saldoContaPoupanca;

    private BigDecimal valorEmprestimo;

    private int prazoMeses;

    private Long solicitadoPor;

    private Instant criadoEm;

    @Enumerated(EnumType.STRING)
    private StatusAnalise resultadoStatus;

    private BigDecimal resultadoProbabilidadeRisco;

    private BigDecimal resultadoThresholdUtilizado;

    private String resultadoVersaoModelo;

    private Instant resultadoAnalisadoEm;

    protected SolicitacaoEmprestimoJpaEntity() {
        // exigido pelo JPA
    }

    public static SolicitacaoEmprestimoJpaEntity deDominio(SolicitacaoEmprestimo solicitacao) {
        SolicitacaoEmprestimoJpaEntity entity = new SolicitacaoEmprestimoJpaEntity();
        entity.id = solicitacao.id();
        entity.idade = solicitacao.cliente().idade();
        entity.salarioAnual = solicitacao.cliente().salarioAnual();
        entity.situacaoMoradia = solicitacao.cliente().situacaoMoradia();
        entity.saldoContaCorrente = solicitacao.cliente().saldoContaCorrente();
        entity.saldoContaPoupanca = solicitacao.cliente().saldoContaPoupanca();
        entity.valorEmprestimo = solicitacao.valorEmprestimo();
        entity.prazoMeses = solicitacao.prazoMeses();
        entity.solicitadoPor = solicitacao.solicitadoPor();
        entity.criadoEm = solicitacao.criadoEm();

        ResultadoAnalise resultado = solicitacao.resultado();
        if (resultado != null) {
            entity.resultadoStatus = resultado.resultado();
            entity.resultadoProbabilidadeRisco = resultado.probabilidadeRisco();
            entity.resultadoThresholdUtilizado = resultado.thresholdUtilizado();
            entity.resultadoVersaoModelo = resultado.versaoModelo();
            entity.resultadoAnalisadoEm = resultado.analisadoEm();
        }
        return entity;
    }

    public SolicitacaoEmprestimo paraDominio() {
        Cliente cliente = new Cliente(idade, salarioAnual, situacaoMoradia, saldoContaCorrente, saldoContaPoupanca);
        ResultadoAnalise resultado = new ResultadoAnalise(
                resultadoStatus, resultadoProbabilidadeRisco, resultadoThresholdUtilizado, resultadoVersaoModelo, resultadoAnalisadoEm
        );
        return new SolicitacaoEmprestimo(id, cliente, valorEmprestimo, prazoMeses, solicitadoPor, criadoEm, resultado);
    }
}
