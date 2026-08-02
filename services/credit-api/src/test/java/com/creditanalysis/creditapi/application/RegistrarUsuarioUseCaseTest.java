package com.creditanalysis.creditapi.application;

import com.creditanalysis.creditapi.domain.exception.EmailJaCadastradoException;
import com.creditanalysis.creditapi.domain.model.Role;
import com.creditanalysis.creditapi.domain.model.Usuario;
import com.creditanalysis.creditapi.domain.port.PasswordEncoder;
import com.creditanalysis.creditapi.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void deve_criar_usuario_analista_com_senha_criptografada() {
        when(repository.existePorEmail("ana@creditanalysis.local")).thenReturn(false);
        when(passwordEncoder.encode("senha1234")).thenReturn("hash-criptografado");
        when(repository.salvar(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            return new Usuario(1L, usuario.nome(), usuario.email(), usuario.senhaHash(), usuario.role(), Instant.now());
        });

        var useCase = new RegistrarUsuarioUseCase(repository, passwordEncoder);
        var usuario = useCase.executar(new ComandoRegistrarUsuario("Ana", "ana@creditanalysis.local", "senha1234"));

        assertThat(usuario.id()).isEqualTo(1L);
        assertThat(usuario.role()).isEqualTo(Role.ANALISTA);
        assertThat(usuario.senhaHash()).isEqualTo("hash-criptografado");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).salvar(captor.capture());
        assertThat(captor.getValue().senhaHash()).isEqualTo("hash-criptografado");
    }

    @Test
    void deve_rejeitar_email_ja_cadastrado_sem_persistir() {
        when(repository.existePorEmail("ana@creditanalysis.local")).thenReturn(true);

        var useCase = new RegistrarUsuarioUseCase(repository, passwordEncoder);

        assertThatThrownBy(() -> useCase.executar(new ComandoRegistrarUsuario("Ana", "ana@creditanalysis.local", "senha1234")))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(repository, never()).salvar(org.mockito.ArgumentMatchers.any());
    }
}
