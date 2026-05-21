package ru.kholodov.locationcontextservice.dto.planner;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kholodov.locationcontextservice.dto.places.PlacesSearchResponse;

import java.util.List;

/**
 * DTO запроса к {@code POST /api/v1/plan/build} (planner-service).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanBuildRequest {
    private String startTime;      // формат "HH:mm"
    private String endTime;        // формат "HH:mm"
    private List<PlacesSearchResponse.PlaceDto> places;
    private Double startLatitude;  // опционально: стартовая точка маршрута
    private Double startLongitude;
}
