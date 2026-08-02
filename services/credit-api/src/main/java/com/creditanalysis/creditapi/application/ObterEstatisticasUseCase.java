package com.creditanalysis.creditapi.application;

import com.creditanalysis.creditapi.domain.model.EstatisticasAnalise;
import com.creditanalysis.creditapi.domain.repository.SolicitacaoEmprestimoRepository;

public class ObterEstatisticasUseCase {

    private final SolicitacaoEmprestimoRepository repository;

    public ObterEstatisticasUseCase(SolicitacaoEmprestimoRepository repository) {
        this.repository = repository;
    }

    public EstatisticasAnalise executar() {
        return repository.calcularEstatisticas();
    }
}
