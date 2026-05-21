package ru.kholodov.locationcontextservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.kholodov.locationcontextservice.dto.planner.PlanBuildResponse;
import ru.kholodov.locationcontextservice.enums.Pace;

import java.time.Duration;
import java.time.LocalTime;

/**
 * DTO с результатом расчёта контекста прогулки и готовым маршрутом.
 *
 * <p>Включает уточнённый адрес, координаты, радиус доступности, доступное время,
 * временные границы, темп движения и опционально — построенный план посещения мест.
 *
 * @param resolvedLocation уточнённый адрес после геокодирования
 * @param latitude широта
 * @param longitude долгота
 * @param radiusMeters радиус пешеходной доступности в метрах
 * @param availableHours доступное время в часах
 * @param startTime время начала прогулки
 * @param endTime время окончания прогулки
 * @param pace темп прогулки
 * @param plan опциональный готовый план с расписанием (может быть {@code null})
 */
@Schema(description = "Результат расчёта контекста прогулки с маршрутом")
public record LocationContextResponse(
        @Schema(description = "Уточнённый адрес после геокодирования", example = "Арбат, Москва")
        String resolvedLocation,

        @Schema(description = "Широта", example = "55.7520") double latitude,

        @Schema(description = "Долгота", example = "37.5921") double longitude,

        @Schema(description = "Радиус пешеходной доступности в метрах", example = "3000")
        int radiusMeters,

        @Schema(description = "Доступное время прогулки в часах", example = "6.0")
        double availableHours,

        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "Время начала прогулки", example = "10:00", type = "string")
        LocalTime startTime,

        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "Время окончания прогулки", example = "16:00", type = "string")
        LocalTime endTime,

        @Schema(description = "Темп прогулки", example = "MEDIUM") Pace pace,

        @Schema(description = "Готовый план маршрута ")
        PlanBuildResponse.PlanData plan) {

    /**
     * Фабричный метод для создания ответа без плана.
     */
    public static LocationContextResponse create(
            String resolvedLocation,
            double latitude,
            double longitude,
            int radiusMeters,
            LocalTime startTime,
            LocalTime endTime,
            Pace pace) {
        double availableHours = Duration.between(startTime, endTime).toMinutes() / 60.0;
        return new LocationContextResponse(
                resolvedLocation, latitude, longitude, radiusMeters,
                availableHours, startTime, endTime, pace, null);
    }

    /**
     * Фабричный метод для создания ответа с готовым планом.
     */
    public static LocationContextResponse withPlan(
            String resolvedLocation,
            double latitude,
            double longitude,
            int radiusMeters,
            LocalTime startTime,
            LocalTime endTime,
            Pace pace,
            PlanBuildResponse.PlanData plan) {
        double availableHours = Duration.between(startTime, endTime).toMinutes() / 60.0;
        return new LocationContextResponse(
                resolvedLocation, latitude, longitude, radiusMeters,
                availableHours, startTime, endTime, pace, plan);
    }
}