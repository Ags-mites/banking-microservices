package com.bank.customerservice.infrastructure.input;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.bank.customerservice.domain.exception.ClienteNotFoundException;
import com.bank.customerservice.domain.exception.ContrasenaInvalidaException;
import com.bank.customerservice.domain.exception.IdentificacionDuplicadaException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final DateTimeFormatter dateFormatter = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        String detail = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        ErrorResponse response = new ErrorResponse(
            "https://example.com/errors/validation-failed",
            "Validation Failed",
            400,
            detail.isEmpty() ? "Datos inválidos" : detail,
            extractPath(request),
            LocalDateTime.now().format(dateFormatter)
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            WebRequest request) {
        
        ErrorResponse response = new ErrorResponse(
            "https://example.com/errors/invalid-request",
            "Bad Request",
            400,
            "El cuerpo de la solicitud está malformado o contiene JSON inválido",
            extractPath(request),
            LocalDateTime.now().format(dateFormatter)
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClienteNotFound(
            ClienteNotFoundException ex,
            WebRequest request) {
        
        ErrorResponse response = new ErrorResponse(
            "https://example.com/errors/cliente-not-found",
            "Not Found",
            404,
            ex.getMessage(),
            extractPath(request),
            LocalDateTime.now().format(dateFormatter)
        );
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(IdentificacionDuplicadaException.class)
    public ResponseEntity<ErrorResponse> handleIdentificacionDuplicada(
            IdentificacionDuplicadaException ex,
            WebRequest request) {
        
        ErrorResponse response = new ErrorResponse(
            "https://example.com/errors/duplicate-identification",
            "Conflict",
            409,
            ex.getMessage(),
            extractPath(request),
            LocalDateTime.now().format(dateFormatter)
        );
        
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(ContrasenaInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleContrasenaInvalida(
            ContrasenaInvalidaException ex,
            WebRequest request) {
        
        ErrorResponse response = new ErrorResponse(
            "https://example.com/errors/invalid-password",
            "Bad Request",
            400,
            ex.getMessage(),
            extractPath(request),
            LocalDateTime.now().format(dateFormatter)
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {
        
        ErrorResponse response = new ErrorResponse(
            "https://example.com/errors/invalid-input",
            "Bad Request",
            400,
            ex.getMessage(),
            extractPath(request),
            LocalDateTime.now().format(dateFormatter)
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.startsWith("uri=") ? description.substring(4) : description;
    }
}
