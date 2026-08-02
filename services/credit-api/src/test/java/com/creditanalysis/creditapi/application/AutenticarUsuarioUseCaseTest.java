package com.creditanalysis.creditapi.application;

import com.creditanalysis.creditapi.domain.exception.CredenciaisInvalidasException;
import com.creditanalysis.creditapi.domain.model.Role;
import com.creditanalysis.creditapi.domain.model.Usuario;
import com.creditanalysis.creditapi.domain.port.GeradorToken;
import com.creditanalysis.creditapi.domain.port.PasswordEncoder;
import com.creditanalysis.creditapi.domain.port.TokenAcesso;
import com.creditanalysis.creditapi.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GeradorToken geradorToken;

    private static Usuario usuario() {
        return new Usuario(1L, "Ana", "ana@creditanalysis.local", "hash", Role.ANALISTA, Instant.now());
    }

    @Test
    void deve_gerar_token_quando_credenciais_sao_validas() {
        var usuario = usuario();
        when(repository.buscarPorEmail("ana@creditanalysis.local")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha1234", "hash")).thenReturn(true);
        var tokenEsperado = new TokenAcesso("jwt-token", Instant.now());
        when(geradorToken.gerar(usuario)).thenReturn(tokenEsperado);

        var useCase = new AutenticarUsuarioUseCase(repository, passwordEncoder, geradorToken);
        var token = useCase.executar(new ComandoAutenticar("ana@creditanalysis.local", "senha1234"));

        assertThat(token).isEqualTo(tokenEsperado);
    }

    @Test
    void deve_rejeitar_quando_usuario_nao_existe() {
        when(repository.buscarPorEmail("desconhecido@creditanalysis.local")).thenReturn(Optional.empty());

        var useCase = new AutenticarUsuarioUseCase(repository, passwordEncoder, geradorToken);

        assertThatThrownBy(() -> useCase.executar(new ComandoAutenticar("desconhecido@creditanalysis.local", "senha1234")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void deve_rejeitar_quando_senha_incorreta() {
        when(repository.buscarPorEmail("ana@creditanalysis.local")).thenReturn(Optional.of(usuario()));
        when(passwordEncoder.matches("senhaerrada", "hash")).thenReturn(false);

        var useCase = new AutenticarUsuarioUseCase(repository, passwordEncoder, geradorToken);

        assertThatThrownBy(() -> useCase.executar(new ComandoAutenticar("ana@creditanalysis.local", "senhaerrada")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }
}
