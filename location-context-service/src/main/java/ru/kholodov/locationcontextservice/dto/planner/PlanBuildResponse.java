package ru.kholodov.locationcontextservice.dto.planner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Ответ от planner-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanBuildResponse {
    private PlanData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlanData {
        private List<PlanItem> items;
        private int totalPlaces;
        private double totalHours;
        private List<String> warnings;
        private int evaluatedOrderings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlanItem {
        private String placeId;
        private String placeName;
        private String arrivalTime;
        private String departureTime;
        private String category;
        private int travelTimeMinutes;
    }
}