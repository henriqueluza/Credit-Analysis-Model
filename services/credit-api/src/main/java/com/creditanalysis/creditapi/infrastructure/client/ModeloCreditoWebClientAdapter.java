package com.creditanalysis.creditapi.infrastructure.client;

import com.creditanalysis.creditapi.domain.exception.ModeloCreditoIndisponivelException;
import com.creditanalysis.creditapi.domain.model.Cliente;
import com.creditanalysis.creditapi.domain.model.StatusAnalise;
import com.creditanalysis.creditapi.domain.port.ModeloCreditoClient;
import com.creditanalysis.creditapi.domain.port.PredicaoRisco;
import com.creditanalysis.creditapi.infrastructure.logging.RequestIdFilter;
import io.netty.channel.ChannelOption;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.time.Duration;

@Component
public class ModeloCreditoWebClientAdapter implements ModeloCreditoClient {

    private final WebClient webClient;

    public ModeloCreditoWebClientAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${ml-service.base-url}") String baseUrl,
            @Value("${ml-service.timeout-ms}") long timeoutMs) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeoutMs)
                .responseTimeout(Duration.ofMillis(timeoutMs));

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
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
