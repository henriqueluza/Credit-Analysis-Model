package com.creditanalysis.creditapi.infrastructure.persistence;

import com.creditanalysis.creditapi.domain.model.EstatisticasAnalise;
import com.creditanalysis.creditapi.domain.model.Pagina;
import com.creditanalysis.creditapi.domain.model.SolicitacaoEmprestimo;
import com.creditanalysis.creditapi.domain.model.StatusAnalise;
import com.creditanalysis.creditapi.domain.repository.SolicitacaoEmprestimoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SolicitacaoEmprestimoRepositoryAdapter implements SolicitacaoEmprestimoRepository {

    private final SolicitacaoEmprestimoJpaRepository jpaRepository;

    public SolicitacaoEmprestimoRepositoryAdapter(SolicitacaoEmprestimoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SolicitacaoEmprestimo salvar(SolicitacaoEmprestimo solicitacao) {
        SolicitacaoEmprestimoJpaEntity salvo = jpaRepository.save(SolicitacaoEmprestimoJpaEntity.deDominio(solicitacao));
        return salvo.paraDominio();
    }

    @Override
    public Optional<SolicitacaoEmprestimo> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(SolicitacaoEmprestimoJpaEntity::paraDominio);
    }

    @Override
    public Pagina<SolicitacaoEmprestimo> buscarHistorico(int pagina, int tamanho) {
        Page<SolicitacaoEmprestimoJpaEntity> resultado = jpaRepository.findAll(
                PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "criadoEm"))
        );
        List<SolicitacaoEmprestimo> conteudo = resultado.getContent().stream()
                .map(SolicitacaoEmprestimoJpaEntity::paraDominio)
                .toList();
        return new Pagina<>(conteudo, pagina, tamanho, resultado.getTotalElements(), resultado.getTotalPages());
    }

    @Override
    public EstatisticasAnalise calcularEstatisticas() {
        long total = jpaRepository.count();
        long aprovados = jpaRepository.countByResultadoStatus(StatusAnalise.APROVADO);
        long reprovados = jpaRepository.countByResultadoStatus(StatusAnalise.REPROVADO);
        double taxaAprovacao = total > 0 ? (aprovados * 100.0) / total : 0.0;
        return new EstatisticasAnalise(total, aprovados, reprovados, taxaAprovacao);
    }
}
