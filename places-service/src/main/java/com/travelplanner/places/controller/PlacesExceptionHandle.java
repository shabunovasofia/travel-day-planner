package com.travelplanner.places.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PlacesExceptionHandle {
  /**
   * Обрабатывает ошибки валидации входящего запроса.
   *
   * @param ex исключение валидации
   * @return ответ с кодом 400 и описанием ошибки
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(
      final MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(java.util.stream.Collectors.joining(", "));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("errors", List.of(Map.of("code", "VALIDATION_ERROR", "message", message))));
  }

  /**
   * Обрабатывает все остальные исключения.
   *
   * @param ex исключение
   * @return ответ с кодом 500 и описанием ошибки
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneral(final Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
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
