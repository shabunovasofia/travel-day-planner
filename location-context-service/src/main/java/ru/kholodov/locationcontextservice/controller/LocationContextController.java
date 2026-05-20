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
import ru.kholodov.locationcontextservice.dto.LocationContextResponse;
import ru.kholodov.locationcontextservice.services.LocationContextService;

/**
 * REST-контроллер для получения контекста прогулки по заданной локации.
 *
 * <p>Принимает запрос с адресом, временным интервалом и темпом движения, возвращает уточнённые
 * координаты, радиус поиска и доступное время.
 *
 * @author Stepan Kholodov
 * @version 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/context")
@Tag(name = "Location Context", description = "Расчёт контекста прогулки: координаты и радиус доступности")
public class LocationContextController {

    private final LocationContextService locationContextService;

    /**
     * Анализирует контекст прогулки по адресу, временному интервалу и темпу.
     *
     * @param request параметры запроса: адрес, время начала/окончания, темп
     * @return контекст прогулки с координатами и радиусом доступности
     */
    @Operation(
            summary = "Анализ контекста прогулки",
            description =
                    "Геокодирует адрес, вычисляет доступное время и возвращает радиус "
                            + "пешеходной доступности через изохрону или fallback-формулу.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Успешный расчёт",
                content =
                        @Content(schema = @Schema(implementation = LocationContextResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации запроса",
                content = @Content(schema = @Schema(example = "{\"error\": \"поле: сообщение\"}"))),
        @ApiResponse(
                responseCode = "404",
                description = "Адрес не найден",
                content =
                        @Content(
                                schema =
                                        @Schema(
                                                example =
                                                        "{\"error\": \"Не удалось найти координаты для адреса: ...\"}"))),
        @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера",
                content = @Content(schema = @Schema(example = "{\"error\": \"Internal error\"}")))
    })
    @PostMapping("/analyze")
    public LocationContextResponse getLocation(
            @Valid @RequestBody LocationContextRequest request) {
        log.info("Вызван метод getLocation для адреса: {}", request.getLocation());
        return locationContextService.getLocation(request);
    }
}
