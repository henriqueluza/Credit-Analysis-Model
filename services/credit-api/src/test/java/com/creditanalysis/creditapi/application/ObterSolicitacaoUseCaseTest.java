package com.creditanalysis.creditapi.application;

import com.creditanalysis.creditapi.domain.exception.SolicitacaoNaoEncontradaException;
import com.creditanalysis.creditapi.domain.model.Cliente;
import com.creditanalysis.creditapi.domain.model.SituacaoMoradia;
import com.creditanalysis.creditapi.domain.model.SolicitacaoEmprestimo;
import com.creditanalysis.creditapi.domain.repository.SolicitacaoEmprestimoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObterSolicitacaoUseCaseTest {

    @Mock
    private SolicitacaoEmprestimoRepository repository;

    @Test
    void deve_retornar_solicitacao_quando_encontrada() {
        var solicitacao = SolicitacaoEmprestimo.nova(
                new Cliente(35, new BigDecimal("60000"), SituacaoMoradia.OWN, new BigDecimal("1500"), new BigDecimal("5000")),
                new BigDecimal("10000"), 24, 1L
        );
        when(repository.buscarPorId(1L)).thenReturn(Optional.of(solicitacao));

        var useCase = new ObterSolicitacaoUseCase(repository);

        assertThat(useCase.executar(1L)).isEqualTo(solicitacao);
    }

    @Test
    void deve_lancar_excecao_quando_nao_encontrada() {
        when(repository.buscarPorId(99L)).thenReturn(Optional.empty());

        var useCase = new ObterSolicitacaoUseCase(repository);

        assertThatThrownBy(() -> useCase.executar(99L))
                .isInstanceOf(SolicitacaoNaoEncontradaException.class);
    }
}
