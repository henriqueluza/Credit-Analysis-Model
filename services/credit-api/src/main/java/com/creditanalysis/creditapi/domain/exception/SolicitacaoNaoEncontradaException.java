package com.creditanalysis.creditapi.domain.exception;

public class SolicitacaoNaoEncontradaException extends RuntimeException {

    public SolicitacaoNaoEncontradaException(Long id) {
        super("Solicitação não encontrada: " + id);
    }
}
