package ru.kholodov.locationcontextservice.dto.places;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO ответа от places-service.
 *
 * <p>Соответствует формату {@code {"data": {"places": [...], "totalFound": N}}}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlacesSearchResponse {
    private DataWrapper data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataWrapper {
        private List<PlaceDto> places;
        private int totalFound;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceDto {
        private String placeId;
        private String name;
        private String category;
        private double latitude;
        private double longitude;
        private double estimatedHours;
        private String description;
        private Double rating;
        private String address;
        private String openingHoursText;
        private boolean scheduleUnknown;
    }
}
