package com.creditanalysis.creditapi.interfaces.web;

import com.creditanalysis.creditapi.application.AnalisarCreditoUseCase;
import com.creditanalysis.creditapi.application.ComandoAnalisarCredito;
import com.creditanalysis.creditapi.application.ListarHistoricoUseCase;
import com.creditanalysis.creditapi.application.ObterEstatisticasUseCase;
import com.creditanalysis.creditapi.application.ObterSolicitacaoUseCase;
import com.creditanalysis.creditapi.domain.model.EstatisticasAnalise;
import com.creditanalysis.creditapi.domain.model.Pagina;
import com.creditanalysis.creditapi.domain.model.SolicitacaoEmprestimo;
import com.creditanalysis.creditapi.interfaces.web.dto.EstatisticasResponse;
import com.creditanalysis.creditapi.interfaces.web.dto.PaginaResponse;
import com.creditanalysis.creditapi.interfaces.web.dto.SolicitacaoAnaliseRequest;
import com.creditanalysis.creditapi.interfaces.web.dto.SolicitacaoAnaliseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analises")
public class AnaliseCreditoController {

    private static final int TAMANHO_PAGINA_MAXIMO = 100;

    private final AnalisarCreditoUseCase analisarCreditoUseCase;
    private final ListarHistoricoUseCase listarHistoricoUseCase;
    private final ObterSolicitacaoUseCase obterSolicitacaoUseCase;
    private final ObterEstatisticasUseCase obterEstatisticasUseCase;

    public AnaliseCreditoController(
            AnalisarCreditoUseCase analisarCreditoUseCase,
            ListarHistoricoUseCase listarHistoricoUseCase,
            ObterSolicitacaoUseCase obterSolicitacaoUseCase,
            ObterEstatisticasUseCase obterEstatisticasUseCase) {
        this.analisarCreditoUseCase = analisarCreditoUseCase;
        this.listarHistoricoUseCase = listarHistoricoUseCase;
        this.obterSolicitacaoUseCase = obterSolicitacaoUseCase;
        this.obterEstatisticasUseCase = obterEstatisticasUseCase;
    }

    @PostMapping
    public ResponseEntity<SolicitacaoAnaliseResponse> analisar(
            @Valid @RequestBody SolicitacaoAnaliseRequest request,
            @AuthenticationPrincipal Long usuarioId) {
        ComandoAnalisarCredito comando = new ComandoAnalisarCredito(
                request.idade(),
                request.salarioAnual(),
                request.situacaoMoradia(),
                request.saldoContaCorrente(),
                request.saldoContaPoupanca(),
                request.valorEmprestimo(),
                request.prazoMeses(),
                usuarioId
        );
        SolicitacaoEmprestimo solicitacao = analisarCreditoUseCase.executar(comando);
        return ResponseEntity.status(HttpStatus.CREATED).body(SolicitacaoAnaliseResponse.deDominio(solicitacao));
    }

    @GetMapping
    public PaginaResponse<SolicitacaoAnaliseResponse> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pagina<SolicitacaoEmprestimo> pagina = listarHistoricoUseCase.executar(page, Math.min(size, TAMANHO_PAGINA_MAXIMO));
        return new PaginaResponse<>(
                pagina.conteudo().stream().map(SolicitacaoAnaliseResponse::deDominio).toList(),
                pagina.pagina(),
                pagina.tamanho(),
                pagina.totalElementos(),
                pagina.totalPaginas()
        );
    }

    @GetMapping("/{id}")
    public SolicitacaoAnaliseResponse obter(@PathVariable Long id) {
        return SolicitacaoAnaliseResponse.deDominio(obterSolicitacaoUseCase.executar(id));
    }

    @GetMapping("/stats")
    public EstatisticasResponse estatisticas() {
        EstatisticasAnalise stats = obterEstatisticasUseCase.executar();
        return new EstatisticasResponse(stats.total(), stats.aprovados(), stats.reprovados(), stats.taxaAprovacao());
    }
}
