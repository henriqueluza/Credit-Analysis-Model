package com.creditanalysis.creditapi.application;

import com.creditanalysis.creditapi.domain.model.EstatisticasAnalise;
import com.creditanalysis.creditapi.domain.repository.SolicitacaoEmprestimoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObterEstatisticasUseCaseTest {

    @Mock
    private SolicitacaoEmprestimoRepository repository;

    @Test
    void deve_delegar_calculo_de_estatisticas_para_o_repositorio() {
        var estatisticas = new EstatisticasAnalise(10, 7, 3, 70.0);
        when(repository.calcularEstatisticas()).thenReturn(estatisticas);

        var useCase = new ObterEstatisticasUseCase(repository);

        assertThat(useCase.executar()).isEqualTo(estatisticas);
    }
}
