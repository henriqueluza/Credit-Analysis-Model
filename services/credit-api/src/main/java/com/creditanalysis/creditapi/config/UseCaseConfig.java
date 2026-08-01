package com.creditanalysis.creditapi.config;

import com.creditanalysis.creditapi.application.AnalisarCreditoUseCase;
import com.creditanalysis.creditapi.application.ListarHistoricoUseCase;
import com.creditanalysis.creditapi.application.ObterEstatisticasUseCase;
import com.creditanalysis.creditapi.application.ObterSolicitacaoUseCase;
import com.creditanalysis.creditapi.domain.port.ModeloCreditoClient;
import com.creditanalysis.creditapi.domain.repository.SolicitacaoEmprestimoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fábrica dos casos de uso — mantém application/domain livres de anotações Spring,
 * com o wiring concentrado nesta borda de configuração.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public AnalisarCreditoUseCase analisarCreditoUseCase(
            ModeloCreditoClient modeloCreditoClient, SolicitacaoEmprestimoRepository repository) {
        return new AnalisarCreditoUseCase(modeloCreditoClient, repository);
    }

    @Bean
    public ListarHistoricoUseCase listarHistoricoUseCase(SolicitacaoEmprestimoRepository repository) {
        return new ListarHistoricoUseCase(repository);
    }

    @Bean
    public ObterSolicitacaoUseCase obterSolicitacaoUseCase(SolicitacaoEmprestimoRepository repository) {
        return new ObterSolicitacaoUseCase(repository);
    }

    @Bean
    public ObterEstatisticasUseCase obterEstatisticasUseCase(SolicitacaoEmprestimoRepository repository) {
        return new ObterEstatisticasUseCase(repository);
    }
}
