package ru.kholodov.locationcontextservice.dto.places;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO запроса к {@code POST /api/v1/places/search} (places-service).
 *
 * <p>Описывает географический «круг» поиска: центр (lat/lon), радиус в метрах и доступное
 * пользователю время. Places-service использует {@code availableHours}, чтобы отфильтровать
 * места, которые заведомо не уместятся в окно прогулки.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacesSearchRequest {

    /** Широта центра поиска. */
    private double latitude;

    /** Долгота центра поиска. */
    private double longitude;

    /** Радиус поиска в метрах. */
    private int radiusMeters;

    /** Доступное пользователю время на всю прогулку, в часах. */
    private double availableHours;
}
