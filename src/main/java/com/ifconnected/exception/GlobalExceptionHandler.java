package com.ifconnected.config;

import com.ifconnected.exception.BusinessRuleException;
import com.ifconnected.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Tratamento para "Recurso não encontrado" (Status 404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Não Encontrado", ex.getMessage());
    }

    // 2. Tratamento para "Regra de Negócio Violada" (Status 422)
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Object> handleBusinessRuleViolation(BusinessRuleException ex) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Erro de Validação", ex.getMessage());
    }

    // 3. O Pega-Tudo antigo (Erros genéricos) (Status 400 ou 500)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Requisição Inválida", ex.getMessage());
    }

    // Método auxiliar para não ficar repetindo código na hora de montar o JSON
    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}