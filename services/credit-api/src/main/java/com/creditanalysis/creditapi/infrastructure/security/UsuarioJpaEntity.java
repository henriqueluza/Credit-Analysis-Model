package com.creditanalysis.creditapi.infrastructure.security;

import com.creditanalysis.creditapi.domain.model.Role;
import com.creditanalysis.creditapi.domain.model.Usuario;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String email;

    private String senhaHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Instant criadoEm;

    protected UsuarioJpaEntity() {
        // exigido pelo JPA
    }

    public static UsuarioJpaEntity deDominio(Usuario usuario) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.id = usuario.id();
        entity.nome = usuario.nome();
        entity.email = usuario.email();
        entity.senhaHash = usuario.senhaHash();
        entity.role = usuario.role();
        entity.criadoEm = usuario.criadoEm();
        return entity;
    }

    public Usuario paraDominio() {
        return new Usuario(id, nome, email, senhaHash, role, criadoEm);
    }
}
