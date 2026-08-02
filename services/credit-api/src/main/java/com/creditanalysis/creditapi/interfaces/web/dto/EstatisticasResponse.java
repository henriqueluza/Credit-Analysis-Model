package com.creditanalysis.creditapi.interfaces.web.dto;

public record EstatisticasResponse(long total, long aprovados, long reprovados, double taxaAprovacao) {
}
