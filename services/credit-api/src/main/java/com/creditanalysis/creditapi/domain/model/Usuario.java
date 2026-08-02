package com.creditanalysis.creditapi.domain.model;

import com.creditanalysis.creditapi.domain.exception.UsuarioInvalidoException;

import java.time.Instant;

public record Usuario(
        Long id,
        String nome,
        String email,
        String senhaHash,
        Role role,
        Instant criadoEm
) {
    public Usuario {
        if (nome == null || nome.isBlank()) {
            throw new UsuarioInvalidoException("Nome é obrigatório");
        }
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new UsuarioInvalidoException("Email inválido");
        }
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new UsuarioInvalidoException("Senha é obrigatória");
        }
    }

    public static Usuario novoAnalista(String nome, String email, String senhaHash) {
        return new Usuario(null, nome, email, senhaHash, Role.ANALISTA, Instant.now());
    }
}
