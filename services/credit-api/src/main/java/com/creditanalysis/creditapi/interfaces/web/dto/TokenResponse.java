package com.creditanalysis.creditapi.interfaces.web.dto;

import java.time.Instant;

public record TokenResponse(String token, String tipo, Instant expiraEm) {
}
