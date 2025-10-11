package com.task10.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace(); // This will show the full error in logs
        return ResponseEntity.internalServerError()
                .body("Error: " + e.getMessage() + ". Check server logs for details.");
    }
}