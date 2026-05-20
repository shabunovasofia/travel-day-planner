package ru.kholodov.locationcontextservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;
import java.time.LocalTime;

import ru.kholodov.locationcontextservice.enums.Pace;

/**
 * DTO с результатом расчёта контекста прогулки.
 *
 * <p>Включает уточнённый адрес, координаты, радиус доступности в метрах, доступное время в часах,
 * временные границы и темп движения.
 *
 * @param resolvedLocation уточнённый адрес после геокодирования
 * @param latitude широта
 * @param longitude долгота
 * @param radiusMeters радиус пешеходной доступности в метрах
 * @param availableHours доступное время в часах (вычисляется из startTime и endTime)
 * @param startTime время начала прогулки
 * @param endTime время окончания прогулки
 * @param pace темп прогулки ({@link Pace})
 */
@Schema(description = "Результат расчёта контекста прогулки")
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
        @Schema(description = "Темп прогулки", example = "MEDIUM") Pace pace) {

    /**
     * Фабричный метод для создания объекта ответа с автоматическим вычислением доступного времени.
     *
     * @param resolvedLocation уточнённый адрес
     * @param latitude широта
     * @param longitude долгота
     * @param radiusMeters уже вычисленный радиус в метрах
     * @param startTime время начала прогулки
     * @param endTime время окончания прогулки
     * @param pace темп прогулки
     * @return новый экземпляр {@link LocationContextResponse} с заполненным полем availableHours
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
                resolvedLocation,
                latitude,
                longitude,
                radiusMeters,
                availableHours,
                startTime,
                endTime,
                pace);
    }
}
