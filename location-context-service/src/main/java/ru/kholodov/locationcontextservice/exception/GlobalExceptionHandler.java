package ru.kholodov.locationcontextservice.exception;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import ru.kholodov.locationcontextservice.dto.ApiErrors;

/**
 * Глобальный обработчик исключений REST API.
 *
 * <p>Перехватывает основные типы исключений и формирует единообразный JSON-ответ
 * {@link ApiErrors} с массивом {@code errors} (каждая ошибка — code + message). HTTP-статусы:
 *
 * <ul>
 *   <li>400 — ошибки валидации, некорректные аргументы, нечитаемый JSON;
 *   <li>404 — адрес не найден;
 *   <li>504 — таймаут или недоступность upstream-сервиса;
 *   <li>500 — внутренние ошибки сервера.
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CODE_VALIDATION = "VALIDATION_ERROR";
    private static final String CODE_BAD_REQUEST = "BAD_REQUEST";
    private static final String CODE_ADDRESS_NOT_FOUND = "ADDRESS_NOT_FOUND";
    private static final String CODE_UPSTREAM_UNAVAILABLE = "UPSTREAM_UNAVAILABLE";
    private static final String CODE_INTERNAL = "INTERNAL_ERROR";

    /**
     * Обрабатывает {@link IllegalArgumentException} (некорректные аргументы).
     *
     * @param ex исключение
     * @return ответ с кодом 400 и сообщением об ошибке
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrors> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(ApiErrors.of(CODE_BAD_REQUEST, ex.getMessage()));
    }

    /**
     * Обрабатывает нечитаемый JSON в теле запроса (например, неверный тип поля, синтаксическая
     * ошибка). Это клиентская ошибка → 400, а не 500.
     *
     * @param ex {@link HttpMessageNotReadableException} от Jackson
     * @return ответ с кодом 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrors> handleJsonParse(HttpMessageNotReadableException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        return ResponseEntity.status(400).body(ApiErrors.of(CODE_BAD_REQUEST, message));
    }

    /**
     * Обрабатывает {@link AddressNotFoundException}: адрес не найден геокодером.
     *
     * @param ex исключение
     * @return ответ с кодом 404
     */
    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ApiErrors> handleAddressNotFound(AddressNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(ApiErrors.of(CODE_ADDRESS_NOT_FOUND, ex.getMessage()));
    }

    /**
     * Обрабатывает ошибки валидации входных данных: каждое поле — отдельный элемент массива
     * {@code errors}, что позволяет клиенту разобрать ошибки по полям без парсинга строки.
     *
     * @param ex {@link MethodArgumentNotValidException} от Spring Validation
     * @return ответ с кодом 400 и списком ошибок по одному на поле
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrors> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiErrors.ApiError> errors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(fe -> new ApiErrors.ApiError(
                                CODE_VALIDATION,
                                fe.getField() + ": " + fe.getDefaultMessage()))
                        .toList();
        if (errors.isEmpty()) {
            errors = List.of(new ApiErrors.ApiError(CODE_VALIDATION, ex.getMessage()));
        }
        return ResponseEntity.status(400).body(new ApiErrors(errors));
    }

    /**
     * Обрабатывает таймауты/недоступность upstream-сервисов (places, planner, геокодер, ORS).
     *
     * @param ex {@link ResourceAccessException}, обычно содержит {@code java.net.SocketTimeoutException}
     * @return ответ с кодом 504 и обобщённым сообщением (детали идут в логи)
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiErrors> handleUpstreamTimeout(ResourceAccessException ex) {
        log.error("Таймаут при вызове upstream-сервиса: {}", ex.getMessage());
        return ResponseEntity.status(504)
                .body(ApiErrors.of(
                        CODE_UPSTREAM_UNAVAILABLE,
                        "Сервис временно недоступен. Попробуйте позже."));
    }

    /**
     * Catch-all для всех остальных исключений.
     *
     * <p>Стектрейс целиком уходит в логи, наружу — обобщённое сообщение без внутренних деталей,
     * чтобы не утекли сведения о реализации.
     *
     * @param ex исключение
     * @return ответ с кодом 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrors> handleGeneral(Exception ex) {
        log.error("Необработанное исключение: ", ex);
        return ResponseEntity.status(500)
                .body(ApiErrors.of(CODE_INTERNAL, "Внутренняя ошибка сервера"));
    }
}
