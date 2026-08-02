package com.creditanalysis.creditapi.infrastructure.client;

import com.creditanalysis.creditapi.domain.exception.ModeloCreditoIndisponivelException;
import com.creditanalysis.creditapi.domain.model.Cliente;
import com.creditanalysis.creditapi.domain.model.StatusAnalise;
import com.creditanalysis.creditapi.domain.port.ModeloCreditoClient;
import com.creditanalysis.creditapi.domain.port.PredicaoRisco;
import com.creditanalysis.creditapi.infrastructure.logging.RequestIdFilter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.math.BigDecimal;

@Component
public class ModeloCreditoWebClientAdapter implements ModeloCreditoClient {

    private final WebClient webClient;

    public ModeloCreditoWebClientAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${ml-service.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public PredicaoRisco avaliar(Cliente cliente, BigDecimal valorEmprestimo, int prazoMeses) {
        SolicitacaoAnaliseRequest request = new SolicitacaoAnaliseRequest(
                cliente.idade(),
                cliente.salarioAnual(),
                cliente.situacaoMoradia().name(),
                cliente.saldoContaCorrente(),
                cliente.saldoContaPoupanca(),
                valorEmprestimo,
                prazoMeses
        );

        String requestId = MDC.get(RequestIdFilter.MDC_KEY);

        ResultadoPredicaoResponse response;
        try {
            response = webClient.post()
                    .uri("/predict")
                    .header(RequestIdFilter.HEADER, requestId)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ResultadoPredicaoResponse.class)
                    .block();
        } catch (WebClientException e) {
            throw new ModeloCreditoIndisponivelException("Falha ao consultar o serviço de ML", e);
        }

        if (response == null) {
            throw new ModeloCreditoIndisponivelException("Resposta vazia do serviço de ML", null);
        }

        return new PredicaoRisco(
                StatusAnalise.valueOf(response.resultado()),
                response.probabilidadeRisco(),
                response.thresholdUtilizado(),
                response.versaoModelo()
        );
    }
}
