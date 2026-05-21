package ru.kholodov.locationcontextservice.dto.places;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO запроса к {@code POST /api/v1/places/search} (places-service).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacesSearchRequest {
    private double latitude;
    private double longitude;
    private int radiusMeters;
    private double availableHours;
    private List<String> categories;
}
