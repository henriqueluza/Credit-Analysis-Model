package com.creditanalysis.creditapi.interfaces.web;

import com.creditanalysis.creditapi.domain.exception.ModeloCreditoIndisponivelException;
import com.creditanalysis.creditapi.domain.exception.SolicitacaoInvalidaException;
import com.creditanalysis.creditapi.domain.exception.SolicitacaoNaoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SolicitacaoNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleNaoEncontrada(SolicitacaoNaoEncontradaException ex) {
        return erro(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SolicitacaoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleInvalida(SolicitacaoInvalidaException ex) {
        return erro(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
        return erro(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(ModeloCreditoIndisponivelException.class)
    public ResponseEntity<Map<String, Object>> handleModeloIndisponivel(ModeloCreditoIndisponivelException ex) {
        return erro(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", mensagem
        ));
    }
}
