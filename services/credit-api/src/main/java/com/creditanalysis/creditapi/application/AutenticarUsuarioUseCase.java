package com.creditanalysis.creditapi.application;

import com.creditanalysis.creditapi.domain.exception.CredenciaisInvalidasException;
import com.creditanalysis.creditapi.domain.model.Usuario;
import com.creditanalysis.creditapi.domain.port.GeradorToken;
import com.creditanalysis.creditapi.domain.port.PasswordEncoder;
import com.creditanalysis.creditapi.domain.port.TokenAcesso;
import com.creditanalysis.creditapi.domain.repository.UsuarioRepository;

public class AutenticarUsuarioUseCase {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final GeradorToken geradorToken;

    public AutenticarUsuarioUseCase(UsuarioRepository repository, PasswordEncoder passwordEncoder, GeradorToken geradorToken) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.geradorToken = geradorToken;
    }

    public TokenAcesso executar(ComandoAutenticar comando) {
        Usuario usuario = repository.buscarPorEmail(comando.email())
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(comando.senha(), usuario.senhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        return geradorToken.gerar(usuario);
    }
}
