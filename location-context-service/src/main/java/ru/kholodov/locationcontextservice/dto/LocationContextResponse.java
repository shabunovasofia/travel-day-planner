package ru.kholodov.locationcontextservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.kholodov.locationcontextservice.enums.Pace;

import java.time.Duration;
import java.time.LocalTime;

/**
 * DTO для ответа с вычисленным контекстом прогулки.
 * <p>
 * Содержит уточнённые координаты, рассчитанный радиус поиска,
 * доступное время и исходные параметры запроса.
 * </p>
 * <p>
 * Объект является неизменяемым (record), создаётся через фабричный метод
 * {@link #create(String, double, double, LocalTime, LocalTime, Pace)}.
 * </p>
 *
 * @param resolvedLocation исходный текстовый адрес
 * @param latitude         широта центральной точки (получена геокодированием) (пока заглушка)
 * @param longitude        долгота центральной точки (пока заглушка)
 * @param radiusMeters     радиус поиска интересных мест в метрах, зависящий от {@code pace}
 * @param availableHours   количество доступных часов (разница между {@code endTime} и {@code startTime})
 * @param startTime        время начала прогулки (копия из запроса)
 * @param endTime          время окончания прогулки (копия из запроса)
 * @param pace             темп прогулки
 */
public record LocationContextResponse(String resolvedLocation, double latitude, double longitude, int radiusMeters,
                                      double availableHours, @JsonFormat(pattern = "HH:mm") LocalTime startTime,
                                      @JsonFormat(pattern = "HH:mm") LocalTime endTime, Pace pace) {



    /**
     * Фабричный метод для создания ответа с автоматическим расчётом радиуса
     * и доступного времени.
     *
     * @param resolvedLocation текстовый адрес (как был передан клиентом)
     * @param latitude         широта от геокодера (пока заглушка)
     * @param longitude        долгота от геокодера (пока заглушка)
     * @param startTime        время начала прогулки
     * @param endTime          время окончания прогулки
     * @param pace             темп движения
     * @return готовый объект {@link LocationContextResponse}
     */

    public static LocationContextResponse create(String resolvedLocation, double latitude,
                                                 double longitude, LocalTime startTime, LocalTime endTime, Pace pace) {
        int radius = switch (pace) {
            case SLOW -> 2000;
            case MEDIUM -> 3000;
            case FAST -> 5000;
        };

        double availableHours = Duration.between(startTime, endTime).toMinutes() / 60.0;

        return new LocationContextResponse(resolvedLocation, latitude, longitude, radius, availableHours,
                startTime, endTime, pace);
    }
}