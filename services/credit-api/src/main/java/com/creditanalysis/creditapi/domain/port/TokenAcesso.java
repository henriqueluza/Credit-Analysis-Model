package com.creditanalysis.creditapi.domain.port;

import java.time.Instant;

public record TokenAcesso(String token, Instant expiraEm) {
}
