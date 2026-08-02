package com.creditanalysis.creditapi.domain.port;

public interface PasswordEncoder {

    String encode(String senhaTextoPuro);

    boolean matches(String senhaTextoPuro, String senhaHash);
}
