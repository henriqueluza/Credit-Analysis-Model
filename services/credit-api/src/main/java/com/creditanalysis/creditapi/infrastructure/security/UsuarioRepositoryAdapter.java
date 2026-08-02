package com.creditanalysis.creditapi.infrastructure.security;

import com.creditanalysis.creditapi.domain.model.Usuario;
import com.creditanalysis.creditapi.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return jpaRepository.save(UsuarioJpaEntity.deDominio(usuario)).paraDominio();
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email).map(UsuarioJpaEntity::paraDominio);
    }

    @Override
    public boolean existePorEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
