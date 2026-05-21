package ru.kholodov.locationcontextservice.dto.places;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO ответа от places-service.
 *
 * <p>Соответствует формату {@code {"data": {"places": [...], "totalFound": N}}}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlacesSearchResponse {

    /** Полезная нагрузка ответа. */
    private DataWrapper data;

    /**
     * Обёртка с массивом мест и общим счётчиком найденных.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataWrapper {

        /** Места, найденные в заданном радиусе. */
        private List<PlaceDto> places;

        /** Сколько всего мест нашлось до пагинации/фильтрации. */
        private int totalFound;
    }

    /**
     * Одна точка интереса (POI) с метаданными для планировщика.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceDto {

        /** Уникальный идентификатор места в источнике (например, OSM). */
        private String placeId;

        /** Название места. */
        private String name;

        /** Категория места (например, {@code museum}, {@code park}). */
        private String category;

        /** Широта. */
        private double latitude;

        /** Долгота. */
        private double longitude;

        /** Рекомендованное время на посещение в часах. */
        private double estimatedHours;

        /** Краткое описание места. */
        private String description;

        /** Рейтинг (если есть в источнике), либо {@code null}. */
        private Double rating;

        /** Адрес места в свободной форме. */
        private String address;

        /** Расписание работы в произвольной текстовой форме. */
        private String openingHoursText;

        /** {@code true}, если расписание работы неизвестно — планировщик игнорирует ограничение. */
        private boolean scheduleUnknown;
    }
}
