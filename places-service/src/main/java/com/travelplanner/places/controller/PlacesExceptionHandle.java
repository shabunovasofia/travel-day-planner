package com.travelplanner.places.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PlacesExceptionHandle {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(java.util.stream.Collectors.joining(", "));
    return ResponseEntity.status(400)
        .body(Map.of("errors", List.of(Map.of("code", "VALIDATION_ERROR", "message", message))));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
    return ResponseEntity.status(500)
        .body(
            Map.of(
                "errors",
                List.of(
                    Map.of(
                        "code",
                        "INTERNAL_ERROR",
                        "message",
                        ex.getMessage() != null ? ex.getMessage() : "Internal server error"))));
  }
}
