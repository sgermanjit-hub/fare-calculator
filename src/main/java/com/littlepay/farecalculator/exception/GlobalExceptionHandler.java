package com.littlepay.farecalculator.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CsvParsingException.class)
    public ResponseEntity<Map<String, String>> handleCsvParsingException(CsvParsingException csvParsingException) {
        log.warn("CSV Parsing Error: {}", csvParsingException.getMessage());
        return ResponseEntity.badRequest().body(Map.of("errorMessage", csvParsingException.getMessage()));
    }

    @ExceptionHandler(InvalidStopException.class)
    public ResponseEntity<Map<String, String>> handleInvalidStopException(InvalidStopException invalidStopException) {
        log.warn("Invalid Stop Error: {}", invalidStopException.getMessage());
        return ResponseEntity.badRequest().body(Map.of("errorMessage", invalidStopException.getMessage()));
    }

    @ExceptionHandler(TripsWriterFailureException.class)
    public ResponseEntity<Map<String, String>> handleTripsWriterFailureException(TripsWriterFailureException tripsWriterFailureException) {
        log.warn("Trips Writer Error: {}", tripsWriterFailureException.getMessage());
        return ResponseEntity.internalServerError().body(Map.of("errorMessage", tripsWriterFailureException.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException) {
        Map<String, String> failureErrors = methodArgumentNotValidException.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> b));
        log.warn("Validation errors: {}", failureErrors);
        return ResponseEntity.badRequest().body(Map.of("errorMessage", "Validation Failed", "details", failureErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception exception) {
        log.error("Server Error: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", exception.getMessage()));
    }
}
