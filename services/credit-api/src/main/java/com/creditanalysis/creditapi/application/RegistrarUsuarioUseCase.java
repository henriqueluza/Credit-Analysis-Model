package com.creditanalysis.creditapi.application;

import com.creditanalysis.creditapi.domain.exception.EmailJaCadastradoException;
import com.creditanalysis.creditapi.domain.model.Usuario;
import com.creditanalysis.creditapi.domain.port.PasswordEncoder;
import com.creditanalysis.creditapi.domain.repository.UsuarioRepository;

public class RegistrarUsuarioUseCase {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public RegistrarUsuarioUseCase(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario executar(ComandoRegistrarUsuario comando) {
        if (repository.existePorEmail(comando.email())) {
            throw new EmailJaCadastradoException(comando.email());
        }

        Usuario usuario = Usuario.novoAnalista(
                comando.nome(),
                comando.email(),
                passwordEncoder.encode(comando.senha())
        );

        return repository.salvar(usuario);
    }
}
