package com.creditanalysis.creditapi.infrastructure.security;

import com.creditanalysis.creditapi.domain.model.Usuario;
import com.creditanalysis.creditapi.domain.port.GeradorToken;
import com.creditanalysis.creditapi.domain.port.TokenAcesso;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenProvider implements GeradorToken {

    private final SecretKey key;
    private final Duration expiracao;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expiracaoMinutos) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracao = Duration.ofMinutes(expiracaoMinutos);
    }

    @Override
    public TokenAcesso gerar(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(expiracao);

        String token = Jwts.builder()
                .subject(usuario.email())
                .claim("role", usuario.role().name())
                .claim("userId", usuario.id())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiraEm))
                .signWith(key)
                .compact();

        return new TokenAcesso(token, expiraEm);
    }

    public Optional<Claims> validarEExtrairClaims(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
