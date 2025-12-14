package com.ifconnected.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Captura o RuntimeException que lançamos no Repository
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        // Monta um JSON bonitinho de erro
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage()); // A mensagem "O e-mail já existe..." vem aqui

        // Retorna HTTP 400 (Bad Request) em vez de 500
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}