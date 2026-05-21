package com.travelplanner.planner.service;

import com.travelplanner.planner.ai.AiPlaceFilter;
import com.travelplanner.planner.dto.PlaceDto;
import com.travelplanner.planner.dto.PlanRequest;
import com.travelplanner.planner.dto.PlanResponse;
import com.travelplanner.planner.planning.RoutePlanning;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PlannerService {

  private final AiPlaceFilter aiPlaceFilter;

  public PlannerService(AiPlaceFilter aiPlaceFilter) {
    this.aiPlaceFilter = aiPlaceFilter;
  }

  public PlanResponse build(PlanRequest request) {
    validateRequest(request);
    LocalTime dayStart = RoutePlanning.parseTime(request.getStartTime(), "startTime");
    LocalTime dayEnd = RoutePlanning.parseTime(request.getEndTime(), "endTime");
    if (!dayStart.isBefore(dayEnd)) {
      throw new IllegalArgumentException("startTime должно быть раньше endTime.");
    }

    List<PlaceDto> places = request.getPlaces();

    double availableHours = java.time.Duration.between(dayStart, dayEnd).toMinutes() / 60.0;
    List<PlaceDto> filteredPlaces = aiPlaceFilter.filter(
        places, request.getStartTime(), request.getEndTime(), availableHours);

    Double startLat = request.getStartLatitude();
    Double startLon = request.getStartLongitude();
    RoutePlanning.OptimizedSchedule optimized =
        RoutePlanning.optimizeOrdering(filteredPlaces, dayStart, dayEnd, startLat, startLon);

    double totalHours = RoutePlanning.sumVisitHours(optimized.outcome().items());

    return new PlanResponse(
        optimized.outcome().items(),
        optimized.outcome().items().size(),
        totalHours,
        optimized.outcome().warnings(),
        optimized.evaluatedOrderings());
  }

  private void validateRequest(PlanRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("Тело запроса не задано.");
    }
    if (request.getStartTime() == null || request.getStartTime().isBlank()) {
      throw new IllegalArgumentException("Поле startTime обязательно.");
    }
    if (request.getEndTime() == null || request.getEndTime().isBlank()) {
      throw new IllegalArgumentException("Поле endTime обязательно.");
    }
    if (request.getPlaces() == null || request.getPlaces().isEmpty()) {
      throw new IllegalArgumentException("Список мест не должен быть пустым.");
    }
  }
}
