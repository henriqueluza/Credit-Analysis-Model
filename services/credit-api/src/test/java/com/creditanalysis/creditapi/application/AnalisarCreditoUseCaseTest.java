package com.creditanalysis.creditapi.application;

import com.creditanalysis.creditapi.domain.model.Cliente;
import com.creditanalysis.creditapi.domain.model.SituacaoMoradia;
import com.creditanalysis.creditapi.domain.model.SolicitacaoEmprestimo;
import com.creditanalysis.creditapi.domain.model.StatusAnalise;
import com.creditanalysis.creditapi.domain.port.ModeloCreditoClient;
import com.creditanalysis.creditapi.domain.port.PredicaoRisco;
import com.creditanalysis.creditapi.domain.repository.SolicitacaoEmprestimoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalisarCreditoUseCaseTest {

    @Mock
    private ModeloCreditoClient modeloCreditoClient;

    @Mock
    private SolicitacaoEmprestimoRepository repository;

    @Test
    void deve_chamar_modelo_de_credito_e_persistir_solicitacao_com_resultado() {
        var comando = new ComandoAnalisarCredito(
                35, new BigDecimal("60000"), SituacaoMoradia.OWN,
                new BigDecimal("1500"), new BigDecimal("5000"),
                new BigDecimal("10000"), 24, 1L
        );

        var predicao = new PredicaoRisco(StatusAnalise.APROVADO, new BigDecimal("0.10"), new BigDecimal("0.35"), "1.0.0");
        when(modeloCreditoClient.avaliar(any(Cliente.class), eq(new BigDecimal("10000")), eq(24))).thenReturn(predicao);

        var solicitacaoSalva = SolicitacaoEmprestimo.nova(
                new Cliente(35, new BigDecimal("60000"), SituacaoMoradia.OWN, new BigDecimal("1500"), new BigDecimal("5000")),
                new BigDecimal("10000"), 24, 1L
        );
        when(repository.salvar(any())).thenReturn(solicitacaoSalva);

        var useCase = new AnalisarCreditoUseCase(modeloCreditoClient, repository);
        var resultado = useCase.executar(comando);

        assertThat(resultado).isEqualTo(solicitacaoSalva);

        ArgumentCaptor<SolicitacaoEmprestimo> captor = ArgumentCaptor.forClass(SolicitacaoEmprestimo.class);
        verify(repository).salvar(captor.capture());

        SolicitacaoEmprestimo enviadaParaSalvar = captor.getValue();
        assertThat(enviadaParaSalvar.resultado().resultado()).isEqualTo(StatusAnalise.APROVADO);
        assertThat(enviadaParaSalvar.resultado().probabilidadeRisco()).isEqualByComparingTo("0.10");
        assertThat(enviadaParaSalvar.resultado().versaoModelo()).isEqualTo("1.0.0");
        assertThat(enviadaParaSalvar.solicitadoPor()).isEqualTo(1L);
    }

    @Test
    void nao_deve_persistir_quando_modelo_de_credito_falha() {
        var comando = new ComandoAnalisarCredito(
                35, new BigDecimal("60000"), SituacaoMoradia.OWN,
                new BigDecimal("1500"), new BigDecimal("5000"),
                new BigDecimal("10000"), 24, 1L
        );

        when(modeloCreditoClient.avaliar(any(Cliente.class), any(), eq(24)))
                .thenThrow(new RuntimeException("indisponível"));

        var useCase = new AnalisarCreditoUseCase(modeloCreditoClient, repository);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> useCase.executar(comando));
        verifyNoInteractions(repository);
    }
}
