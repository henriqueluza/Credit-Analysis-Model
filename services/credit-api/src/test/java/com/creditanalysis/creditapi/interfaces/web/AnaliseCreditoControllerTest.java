package com.creditanalysis.creditapi.interfaces.web;

import com.creditanalysis.creditapi.application.AnalisarCreditoUseCase;
import com.creditanalysis.creditapi.application.ListarHistoricoUseCase;
import com.creditanalysis.creditapi.application.ObterEstatisticasUseCase;
import com.creditanalysis.creditapi.application.ObterSolicitacaoUseCase;
import com.creditanalysis.creditapi.config.SecurityConfig;
import com.creditanalysis.creditapi.domain.exception.SolicitacaoNaoEncontradaException;
import com.creditanalysis.creditapi.domain.model.Cliente;
import com.creditanalysis.creditapi.domain.model.EstatisticasAnalise;
import com.creditanalysis.creditapi.domain.model.Pagina;
import com.creditanalysis.creditapi.domain.model.ResultadoAnalise;
import com.creditanalysis.creditapi.domain.model.SituacaoMoradia;
import com.creditanalysis.creditapi.domain.model.SolicitacaoEmprestimo;
import com.creditanalysis.creditapi.domain.model.StatusAnalise;
import com.creditanalysis.creditapi.infrastructure.security.JwtAuthenticationFilter;
import com.creditanalysis.creditapi.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnaliseCreditoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
class AnaliseCreditoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalisarCreditoUseCase analisarCreditoUseCase;

    @MockitoBean
    private ListarHistoricoUseCase listarHistoricoUseCase;

    @MockitoBean
    private ObterSolicitacaoUseCase obterSolicitacaoUseCase;

    @MockitoBean
    private ObterEstatisticasUseCase obterEstatisticasUseCase;

    private static UsernamePasswordAuthenticationToken authAnalista() {
        return new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_ANALISTA")));
    }

    private static UsernamePasswordAuthenticationToken authAdmin() {
        return new UsernamePasswordAuthenticationToken(2L, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private static SolicitacaoEmprestimo solicitacaoExemplo() {
        var cliente = new Cliente(35, new BigDecimal("60000"), SituacaoMoradia.OWN, new BigDecimal("1500"), new BigDecimal("5000"));
        var resultado = new ResultadoAnalise(StatusAnalise.APROVADO, new BigDecimal("0.10"), new BigDecimal("0.35"), "1.0.0", Instant.now());
        return SolicitacaoEmprestimo.nova(cliente, new BigDecimal("10000"), 24, 1L).comResultado(resultado);
    }

    @Test
    void deve_retornar_401_sem_autenticacao() throws Exception {
        mockMvc.perform(get("/api/analises"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deve_criar_analise_com_usuario_autenticado_como_solicitante() throws Exception {
        when(analisarCreditoUseCase.executar(any())).thenReturn(solicitacaoExemplo());

        mockMvc.perform(post("/api/analises")
                        .with(authentication(authAnalista()))
                        .contentType("application/json")
                        .content("""
                                {
                                  "idade": 35,
                                  "salarioAnual": 60000.0,
                                  "situacaoMoradia": "OWN",
                                  "saldoContaCorrente": 1500.0,
                                  "saldoContaPoupanca": 5000.0,
                                  "valorEmprestimo": 10000.0,
                                  "prazoMeses": 24
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultado").value("APROVADO"));

        verify(analisarCreditoUseCase).executar(argThatUsuarioIdIgual(1L));
    }

    @Test
    void deve_retornar_422_para_idade_invalida() throws Exception {
        mockMvc.perform(post("/api/analises")
                        .with(authentication(authAnalista()))
                        .contentType("application/json")
                        .content("""
                                {
                                  "idade": 10,
                                  "salarioAnual": 60000.0,
                                  "situacaoMoradia": "OWN",
                                  "saldoContaCorrente": 1500.0,
                                  "saldoContaPoupanca": 5000.0,
                                  "valorEmprestimo": 10000.0,
                                  "prazoMeses": 24
                                }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deve_retornar_404_quando_solicitacao_nao_encontrada() throws Exception {
        when(obterSolicitacaoUseCase.executar(99L)).thenThrow(new SolicitacaoNaoEncontradaException(99L));

        mockMvc.perform(get("/api/analises/99").with(authentication(authAnalista())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deve_listar_historico_paginado() throws Exception {
        var pagina = new Pagina<>(List.of(solicitacaoExemplo()), 0, 10, 1L, 1);
        when(listarHistoricoUseCase.executar(eq(0), eq(10))).thenReturn(pagina);

        mockMvc.perform(get("/api/analises").with(authentication(authAnalista())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].resultado").value("APROVADO"));
    }

    @Test
    void analista_nao_pode_acessar_estatisticas() throws Exception {
        mockMvc.perform(get("/api/analises/stats").with(authentication(authAnalista())))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_pode_acessar_estatisticas() throws Exception {
        when(obterEstatisticasUseCase.executar()).thenReturn(new EstatisticasAnalise(10, 7, 3, 70.0));

        mockMvc.perform(get("/api/analises/stats").with(authentication(authAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taxaAprovacao").value(70.0));
    }

    private static com.creditanalysis.creditapi.application.ComandoAnalisarCredito argThatUsuarioIdIgual(Long usuarioId) {
        return org.mockito.ArgumentMatchers.argThat(comando -> comando.usuarioId().equals(usuarioId));
    }
}
