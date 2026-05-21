package ru.kholodov.locationcontextservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.planner.PlanBuildResponse;
import ru.kholodov.locationcontextservice.services.LocationContextService;

/**
 * REST-контроллер для построения готового маршрута прогулки.
 *
 * <p>Принимает запрос с адресом, временным интервалом и темпом движения,
 * возвращает оптимизированный план посещения мест.
 *
 * @author Stepan Kholodov
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/route")
@Tag(name = "Route Planning", description = "Построение готового маршрута прогулки")
public class LocationContextController {

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
                    "Геокодирует адрес, находит достопримечательности в радиусе пешей доступности " +
                            "и строит оптимальное расписание посещения с учётом времени в пути и графика работы.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Маршрут успешно построен",
                    content = @Content(schema = @Schema(implementation = PlanBuildResponse.PlanData.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации запроса",
                    content = @Content(schema = @Schema(example = "{\"error\": \"поле: сообщение\"}"))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Адрес не найден",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Адрес не найден\"}"))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Internal error\"}")))
    })
    @PostMapping("/build")
    public PlanBuildResponse.PlanData buildRoute(
            @Valid @RequestBody LocationContextRequest request) {
        log.info("Построение маршрута для адреса: {}", request.getLocation());
        return locationContextService.buildRoute(request);
    }
}