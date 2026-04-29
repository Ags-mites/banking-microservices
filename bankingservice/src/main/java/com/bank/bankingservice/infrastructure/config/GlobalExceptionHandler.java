package com.bank.bankingservice.infrastructure.config;

import com.bank.bankingservice.domain.exception.AccountConflictException;
import com.bank.bankingservice.domain.exception.AccountNotFoundException;
import com.bank.bankingservice.domain.exception.AccountValidationException;
import com.bank.bankingservice.domain.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, Object>> handleDomainException(DomainException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of(
                "type", "https://api.example.com/errors/" + ex.getCode().toLowerCase(),
                "title", determineTitle(ex.getStatus()),
                "status", ex.getStatus(),
                "detail", ex.getMessage(),
                "instance", request.getRequestURI()
        ));
    }

    private String determineTitle(int status) {
        if (status == 400) return "Bad Request";
        if (status == 404) return "Not Found";
        if (status == 409) return "Conflict";
        return "Internal Server Error";
    }
}
