package ru.kholodov.locationcontextservice.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Глобальный обработчик исключений REST API.
 *
 * <p>Перехватывает основные типы исключений и формирует единообразный JSON-ответ
 * с полем {@code error}. Возвращает соответствующие HTTP-статусы:
 * <ul>
 *   <li>400 — ошибки валидации, некорректные аргументы</li>
 *   <li>404 — адрес не найден</li>
 *   <li>500 — внутренние ошибки сервера</li>
 * </ul>
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение {@link IllegalArgumentException} (некорректные аргументы).
     *
     * @param ex исключение
     * @return ResponseEntity с кодом 400 и сообщением об ошибке
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
    }

    /**
     * Обрабатывает все непредусмотренные исключения.
     *
     * @param ex исключение
     * @return ResponseEntity с кодом 500 и сообщением об ошибке
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
    }

    /**
     * Обрабатывает исключение {@link AddressNotFoundException} — адрес не найден.
     *
     * @param ex исключение
     * @return ResponseEntity с кодом 404 и сообщением об ошибке
     */
    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAddressNotFound(AddressNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }

    /**
     * Обрабатывает ошибки валидации входных данных.
     *
     * @param ex исключение {@link MethodArgumentNotValidException}
     * @return ResponseEntity с кодом 400 и перечнем полей с сообщениями об ошибках
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> e.getField() + ": " + e.getDefaultMessage())
                        .collect(Collectors.joining(", "));
        return ResponseEntity.status(400).body(Map.of("error", message));
    }
}
