package com.creditanalysis.creditapi.domain.repository;

import com.creditanalysis.creditapi.domain.model.EstatisticasAnalise;
import com.creditanalysis.creditapi.domain.model.Pagina;
import com.creditanalysis.creditapi.domain.model.SolicitacaoEmprestimo;

import java.util.Optional;

public interface SolicitacaoEmprestimoRepository {

    SolicitacaoEmprestimo salvar(SolicitacaoEmprestimo solicitacao);

    Optional<SolicitacaoEmprestimo> buscarPorId(Long id);

    Pagina<SolicitacaoEmprestimo> buscarHistorico(int pagina, int tamanho);

    EstatisticasAnalise calcularEstatisticas();
}
