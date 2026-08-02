package com.creditanalysis.creditapi.application;

import com.creditanalysis.creditapi.domain.model.Pagina;
import com.creditanalysis.creditapi.domain.model.SolicitacaoEmprestimo;
import com.creditanalysis.creditapi.domain.repository.SolicitacaoEmprestimoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarHistoricoUseCaseTest {

    @Mock
    private SolicitacaoEmprestimoRepository repository;

    @Test
    void deve_delegar_para_o_repositorio_com_pagina_e_tamanho() {
        var paginaEsperada = new Pagina<SolicitacaoEmprestimo>(List.of(), 2, 20, 42L, 3);
        when(repository.buscarHistorico(2, 20)).thenReturn(paginaEsperada);

        var useCase = new ListarHistoricoUseCase(repository);
        var resultado = useCase.executar(2, 20);

        assertThat(resultado).isEqualTo(paginaEsperada);
    }
}
