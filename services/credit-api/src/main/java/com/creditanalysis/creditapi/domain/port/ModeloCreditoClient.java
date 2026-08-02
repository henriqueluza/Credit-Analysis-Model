package com.creditanalysis.creditapi.domain.port;

import com.creditanalysis.creditapi.domain.model.Cliente;

import java.math.BigDecimal;

/**
 * Porta para o subdomínio de ML (microsserviço FastAPI) — abstrai "algo que
 * recebe um cliente e um pedido de empréstimo e devolve uma predição de risco".
 */
public interface ModeloCreditoClient {

    PredicaoRisco avaliar(Cliente cliente, BigDecimal valorEmprestimo, int prazoMeses);
}
