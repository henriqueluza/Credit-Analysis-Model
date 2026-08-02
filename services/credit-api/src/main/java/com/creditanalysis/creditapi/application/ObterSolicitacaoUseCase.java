package com.creditanalysis.creditapi.application;

import com.creditanalysis.creditapi.domain.exception.SolicitacaoNaoEncontradaException;
import com.creditanalysis.creditapi.domain.model.SolicitacaoEmprestimo;
import com.creditanalysis.creditapi.domain.repository.SolicitacaoEmprestimoRepository;

public class ObterSolicitacaoUseCase {

    private final SolicitacaoEmprestimoRepository repository;

    public ObterSolicitacaoUseCase(SolicitacaoEmprestimoRepository repository) {
        this.repository = repository;
    }

    public SolicitacaoEmprestimo executar(Long id) {
        return repository.buscarPorId(id).orElseThrow(() -> new SolicitacaoNaoEncontradaException(id));
    }
}
