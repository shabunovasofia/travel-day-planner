package ru.kholodov.locationcontextservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kholodov.locationcontextservice.dto.ApiErrors;
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.planner.PlanBuildResponse;
import ru.kholodov.locationcontextservice.services.LocationContextService;

/**
 * REST-контроллер для построения готового маршрута прогулки.
 *
 * <p>Принимает запрос с адресом, временным интервалом и темпом движения, возвращает
 * оптимизированный план посещения мест.
 *
 * @author Stepan Kholodov
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/route")
@Tag(name = "Route Planning", description = "Построение готового маршрута прогулки")
public class LocationContextController {

  private static final String EX_VALIDATION =
      """
      {
        "errors": [
          {"code": "VALIDATION_ERROR", "message": "location: Адрес не может быть пустым"},
          {"code": "VALIDATION_ERROR", "message": "pace: Темп обязателен"}
        ]
      }""";

  private static final String EX_BAD_JSON =
      """
      {
        "errors": [
          {"code": "BAD_REQUEST", "message": "JSON parse error: ..."}
        ]
      }""";

  private static final String EX_ADDRESS_NOT_FOUND =
      """
      {
        "errors": [
          {"code": "ADDRESS_NOT_FOUND", "message": "Не удалось найти координаты для адреса: ..."}
        ]
      }""";

  private static final String EX_UPSTREAM_UNAVAILABLE =
      """
      {
        "errors": [
          {"code": "UPSTREAM_UNAVAILABLE", "message": "Сервис временно недоступен. Попробуйте позже."}
        ]
      }""";

  private static final String EX_INTERNAL_ERROR =
      """
      {
        "errors": [
          {"code": "INTERNAL_ERROR", "message": "Внутренняя ошибка сервера"}
        ]
      }""";

  private final LocationContextService locationContextService;

  /**
   * Строит готовый маршрут прогулки по заданным параметрам.
   *
   * @param request параметры: адрес, время начала/окончания, темп
   * @return готовый план с расписанием посещения мест
   */
  @Operation(
      summary = "Построить маршрут прогулки",
      description =
          "Геокодирует адрес, находит достопримечательности в радиусе пешей доступности "
              + "и строит оптимальное расписание посещения с учётом времени в пути "
              + "и графика работы.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Маршрут успешно построен",
        content = @Content(schema = @Schema(implementation = PlanBuildResponse.PlanData.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Ошибка валидации запроса или некорректный JSON",
        content =
            @Content(
                schema = @Schema(implementation = ApiErrors.class),
                examples = {
                  @ExampleObject(
                      name = "Validation error",
                      summary = "Валидация полей",
                      value = EX_VALIDATION),
                  @ExampleObject(
                      name = "Bad JSON",
                      summary = "Нечитаемый JSON",
                      value = EX_BAD_JSON)
                })),
    @ApiResponse(
        responseCode = "404",
        description = "Адрес не найден",
        content =
            @Content(
                schema = @Schema(implementation = ApiErrors.class),
                examples =
                    @ExampleObject(name = "Address not found", value = EX_ADDRESS_NOT_FOUND))),
    @ApiResponse(
        responseCode = "504",
        description = "Таймаут или недоступность upstream-сервисов",
        content =
            @Content(
                schema = @Schema(implementation = ApiErrors.class),
                examples =
                    @ExampleObject(
                        name = "Upstream unavailable",
                        value = EX_UPSTREAM_UNAVAILABLE))),
    @ApiResponse(
        responseCode = "500",
        description = "Внутренняя ошибка сервера",
        content =
            @Content(
                schema = @Schema(implementation = ApiErrors.class),
                examples = @ExampleObject(name = "Internal error", value = EX_INTERNAL_ERROR)))
  })
  @PostMapping("/build")
  public PlanBuildResponse.PlanData buildRoute(@Valid @RequestBody LocationContextRequest request) {
    log.info("Построение маршрута для адреса: {}", request.getLocation());
    return locationContextService.buildRoute(request);
  }
}
