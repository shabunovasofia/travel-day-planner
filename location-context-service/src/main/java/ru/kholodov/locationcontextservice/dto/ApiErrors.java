package ru.kholodov.locationcontextservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Единый формат ответа об ошибке для всех HTTP-эндпоинтов сервиса.
 *
 * <p>Формат позволяет вернуть несколько ошибок за один ответ — например, при валидации сразу
 * нескольких полей запроса. Каждая ошибка имеет машинно-читаемый {@link ApiError#code} и
 * человекочитаемое {@link ApiError#message}.
 *
 * <p>Пример сериализации:
 *
 * <pre>
 * {
 *   "errors": [
 *     { "code": "VALIDATION_ERROR", "message": "location: Адрес не может быть пустым" }
 *   ]
 * }
 * </pre>
 *
 * @param errors список ошибок (всегда непустой)
 */
@Schema(description = "Единообразный формат ошибки REST API")
public record ApiErrors(
    @Schema(description = "Список ошибок (всегда непустой)") List<ApiError> errors) {

  /**
   * Удобный конструктор для случая одной ошибки.
   *
   * @param code машинно-читаемый код
   * @param message человекочитаемое сообщение
   * @return объект {@link ApiErrors} с единственной ошибкой в списке
   */
  public static ApiErrors of(String code, String message) {
    return new ApiErrors(List.of(new ApiError(code, message)));
  }

  /**
   * Одна ошибка с кодом и сообщением.
   *
   * @param code машинно-читаемый код (например, {@code VALIDATION_ERROR}, {@code
   *     ADDRESS_NOT_FOUND})
   * @param message человекочитаемое сообщение
   */
  @Schema(description = "Одна ошибка")
  public record ApiError(
      @Schema(description = "Машинно-читаемый код ошибки", example = "ADDRESS_NOT_FOUND")
          String code,
      @Schema(description = "Человекочитаемое сообщение", example = "Адрес не найден")
          String message) {}
}
