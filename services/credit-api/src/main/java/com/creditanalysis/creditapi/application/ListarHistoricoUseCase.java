package com.creditanalysis.creditapi.application;

import com.creditanalysis.creditapi.domain.model.Pagina;
import com.creditanalysis.creditapi.domain.model.SolicitacaoEmprestimo;
import com.creditanalysis.creditapi.domain.repository.SolicitacaoEmprestimoRepository;

public class ListarHistoricoUseCase {

    private final SolicitacaoEmprestimoRepository repository;

    public ListarHistoricoUseCase(SolicitacaoEmprestimoRepository repository) {
        this.repository = repository;
    }

    public Pagina<SolicitacaoEmprestimo> executar(int pagina, int tamanho) {
        return repository.buscarHistorico(pagina, tamanho);
    }
}
