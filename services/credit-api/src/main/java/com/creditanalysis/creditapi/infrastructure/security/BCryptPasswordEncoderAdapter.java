package com.creditanalysis.creditapi.infrastructure.security;

import com.creditanalysis.creditapi.domain.port.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {

    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder delegate =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

    @Override
    public String encode(String senhaTextoPuro) {
        return delegate.encode(senhaTextoPuro);
    }

    @Override
    public boolean matches(String senhaTextoPuro, String senhaHash) {
        return delegate.matches(senhaTextoPuro, senhaHash);
    }
}
