package com.creditanalysis.creditapi.infrastructure.client;

import com.creditanalysis.creditapi.domain.exception.ModeloCreditoIndisponivelException;
import com.creditanalysis.creditapi.domain.model.Cliente;
import com.creditanalysis.creditapi.domain.model.SituacaoMoradia;
import com.creditanalysis.creditapi.domain.model.StatusAnalise;
import com.creditanalysis.creditapi.domain.port.PredicaoRisco;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Sobe um servidor HTTP real (java.net.httpserver, sem dependências externas)
 * simulando o microsserviço de ML, para testar timeout e resposta de erro —
 * cenários que só se manifestam contra um serviço remoto de verdade.
 */
class ModeloCreditoWebClientAdapterTest {

    private HttpServer servidorFalso;

    @AfterEach
    void pararServidor() {
        if (servidorFalso != null) {
            servidorFalso.stop(0);
        }
    }

    private String iniciarServidor(HttpHandler handler) throws IOException {
        servidorFalso = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidorFalso.createContext("/predict", handler);
        servidorFalso.start();
        return "http://localhost:" + servidorFalso.getAddress().getPort();
    }

    private static Cliente clienteExemplo() {
        return new Cliente(35, new BigDecimal("60000"), SituacaoMoradia.OWN, new BigDecimal("1500"), new BigDecimal("5000"));
    }

    private static ModeloCreditoWebClientAdapter criarAdapter(String baseUrl, long timeoutMs) {
        return new ModeloCreditoWebClientAdapter(WebClient.builder(), baseUrl, timeoutMs);
    }

    private static void responder(com.sun.net.httpserver.HttpExchange exchange, int status, String corpo) throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @Test
    void deve_retornar_predicao_quando_ml_service_responde_normalmente() throws IOException {
        String baseUrl = iniciarServidor(exchange -> responder(exchange, 200, """
                {"resultado":"APROVADO","probabilidadeRisco":0.10,"thresholdUtilizado":0.35,"versaoModelo":"1.0.0"}
                """));

        var adapter = criarAdapter(baseUrl, 2000);
        PredicaoRisco predicao = adapter.avaliar(clienteExemplo(), new BigDecimal("10000"), 24);

        assertThat(predicao.resultado()).isEqualTo(StatusAnalise.APROVADO);
        assertThat(predicao.versaoModelo()).isEqualTo("1.0.0");
    }

    @Test
    void deve_lancar_excecao_quando_ml_service_demora_mais_que_o_timeout() throws IOException {
        String baseUrl = iniciarServidor(exchange -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            responder(exchange, 200, """
                    {"resultado":"APROVADO","probabilidadeRisco":0.10,"thresholdUtilizado":0.35,"versaoModelo":"1.0.0"}
                    """);
        });

        var adapter = criarAdapter(baseUrl, 200);

        assertThatThrownBy(() -> adapter.avaliar(clienteExemplo(), new BigDecimal("10000"), 24))
                .isInstanceOf(ModeloCreditoIndisponivelException.class);
    }

    @Test
    void deve_lancar_excecao_quando_ml_service_retorna_erro() throws IOException {
        String baseUrl = iniciarServidor(exchange -> responder(exchange, 500, "{}"));

        var adapter = criarAdapter(baseUrl, 2000);

        assertThatThrownBy(() -> adapter.avaliar(clienteExemplo(), new BigDecimal("10000"), 24))
                .isInstanceOf(ModeloCreditoIndisponivelException.class);
    }
}
