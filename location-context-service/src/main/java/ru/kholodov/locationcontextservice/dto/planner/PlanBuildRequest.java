package ru.kholodov.locationcontextservice.dto.planner;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kholodov.locationcontextservice.dto.places.PlacesSearchResponse;

/**
 * DTO запроса к {@code POST /api/v1/plan/build} (planner-service).
 *
 * <p>Содержит временные рамки прогулки и список кандидатов от places-service. Опционально
 * передаётся стартовая точка — если не задана, планировщик начинает с первого места в списке.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanBuildRequest {

    /** Время начала прогулки в формате {@code HH:mm}. */
    private String startTime;

    /** Время окончания прогулки в формате {@code HH:mm}. */
    private String endTime;

    /** Кандидаты на посещение, полученные от places-service. */
    private List<PlacesSearchResponse.PlaceDto> places;

    /** Широта стартовой точки маршрута (опционально). */
    private Double startLatitude;

    /** Долгота стартовой точки маршрута (опционально). */
    private Double startLongitude;
}
