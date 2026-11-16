package com.example.issuersim.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                "responseCode", "12",
                "responseMessage", e.getReason() != null ? e.getReason() : "Bad Request"
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "responseCode", "12",
                "responseMessage", "Invalid request: " + e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generic(Exception e) {
        return ResponseEntity.status(500).body(Map.of(
                "responseCode", "96",
                "responseMessage", "System malfunction: " + e.getMessage()));
    }
}