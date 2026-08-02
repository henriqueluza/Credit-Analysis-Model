package com.creditanalysis.creditapi.domain.repository;

import com.creditanalysis.creditapi.domain.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorEmail(String email);

    boolean existePorEmail(String email);
}
