package com.creditanalysis.creditapi.interfaces.web;

import com.creditanalysis.creditapi.application.AutenticarUsuarioUseCase;
import com.creditanalysis.creditapi.application.RegistrarUsuarioUseCase;
import com.creditanalysis.creditapi.config.SecurityConfig;
import com.creditanalysis.creditapi.domain.exception.CredenciaisInvalidasException;
import com.creditanalysis.creditapi.domain.exception.EmailJaCadastradoException;
import com.creditanalysis.creditapi.domain.model.Role;
import com.creditanalysis.creditapi.domain.model.Usuario;
import com.creditanalysis.creditapi.domain.port.TokenAcesso;
import com.creditanalysis.creditapi.infrastructure.security.JwtAuthenticationFilter;
import com.creditanalysis.creditapi.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @MockitoBean
    private AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    @Test
    void deve_registrar_usuario_como_analista() throws Exception {
        when(registrarUsuarioUseCase.executar(any()))
                .thenReturn(new Usuario(1L, "Ana", "ana@creditanalysis.local", "hash", Role.ANALISTA, Instant.now()));

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"nome":"Ana","email":"ana@creditanalysis.local","senha":"senha1234"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ANALISTA"))
                .andExpect(jsonPath("$.email").value("ana@creditanalysis.local"));
    }

    @Test
    void deve_retornar_422_para_senha_curta() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"nome":"Ana","email":"ana@creditanalysis.local","senha":"123"}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deve_retornar_409_para_email_ja_cadastrado() throws Exception {
        when(registrarUsuarioUseCase.executar(any()))
                .thenThrow(new EmailJaCadastradoException("ana@creditanalysis.local"));

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"nome":"Ana","email":"ana@creditanalysis.local","senha":"senha1234"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void deve_autenticar_e_retornar_token() throws Exception {
        when(autenticarUsuarioUseCase.executar(any()))
                .thenReturn(new TokenAcesso("jwt-token", Instant.parse("2026-08-01T12:00:00Z")));

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"ana@creditanalysis.local","senha":"senha1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void deve_retornar_401_para_credenciais_invalidas() throws Exception {
        when(autenticarUsuarioUseCase.executar(any())).thenThrow(new CredenciaisInvalidasException());

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"ana@creditanalysis.local","senha":"errada"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
