package com.travelplanner.planner.service;

import com.travelplanner.planner.dto.PlaceDto;
import com.travelplanner.planner.dto.PlanRequest;
import com.travelplanner.planner.dto.PlanResponse;
import com.travelplanner.planner.integration.PlannerIntegrationProperties;
import com.travelplanner.planner.integration.PlannerUpstreamClient;
import com.travelplanner.planner.integration.dto.LocationContextAnalyzeResponse;
import com.travelplanner.planner.integration.dto.WalkPace;
import com.travelplanner.planner.planning.RoutePlanning;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class PlannerService {

  private final PlannerIntegrationProperties integrationProperties;
  private final ObjectProvider<PlannerUpstreamClient> upstreamClient;

  public PlannerService(
      PlannerIntegrationProperties integrationProperties,
      ObjectProvider<PlannerUpstreamClient> upstreamClient) {
    this.integrationProperties = integrationProperties;
    this.upstreamClient = upstreamClient;
  }

  public PlanResponse build(PlanRequest request) {
    validateRequest(request);
    LocalTime dayStart = RoutePlanning.parseTime(request.getStartTime(), "startTime");
    LocalTime dayEnd = RoutePlanning.parseTime(request.getEndTime(), "endTime");
    if (!dayStart.isBefore(dayEnd)) {
      throw new IllegalArgumentException("startTime должно быть раньше endTime.");
    }

    List<String> warnings = new ArrayList<>();
    List<PlaceDto> places = resolvePlaces(request, dayStart, dayEnd, warnings);
    if (places.isEmpty()) {
      throw new IllegalArgumentException("Список мест не должен быть пустым.");
    }

    Double startLat = request.getStartLatitude();
    Double startLon = request.getStartLongitude();
    RoutePlanning.OptimizedSchedule optimized =
        RoutePlanning.optimizeOrdering(places, dayStart, dayEnd, startLat, startLon);
    warnings.addAll(optimized.outcome().warnings());

    double totalHours = RoutePlanning.sumVisitHours(optimized.outcome().items());

    return new PlanResponse(
        optimized.outcome().items(),
        optimized.outcome().items().size(),
        totalHours,
        warnings,
        optimized.evaluatedOrderings());
  }

  private List<PlaceDto> resolvePlaces(
      PlanRequest request, LocalTime dayStart, LocalTime dayEnd, List<String> warnings) {
    List<PlaceDto> merged = new ArrayList<>();
    if (request.getPlaces() != null) {
      merged.addAll(request.getPlaces());
    }
    String walkLocation = request.getWalkLocation();
    if (walkLocation == null || walkLocation.isBlank()) {
      return merged;
    }
    PlannerUpstreamClient client = upstreamClient.getIfAvailable();
    if (!integrationProperties.isEnabled() || client == null) {
      warnings.add(
          "Интеграция с location-context/places отключена — используются только места из запроса.");
      return merged;
    }
    try {
      WalkPace pace = PlannerUpstreamClient.parseWalkPace(request.getWalkPace());
      LocationContextAnalyzeResponse ctx =
          client.analyzeLocation(walkLocation, dayStart, dayEnd, pace);
      List<String> categories =
          PlannerUpstreamClient.nonEmptyCategories(request.getDiscoverCategories());
      if (categories.isEmpty()) {
        warnings.add(
            "Указан walkLocation, но discoverCategories пуст — внешний поиск мест не выполнялся.");
        return merged;
      }
      List<PlaceDto> discovered =
          client.searchPlaces(
              ctx.getLatitude(),
              ctx.getLongitude(),
              ctx.getRadiusMeters(),
              ctx.getAvailableHours(),
              categories);
      int before = merged.size();
      mergeByPlaceId(merged, discovered);
      int added = merged.size() - before;
      if (added > 0) {
        warnings.add("Интеграция places: добавлено новых мест в план: " + added + ".");
      }
    } catch (RestClientException ex) {
      warnings.add("Ошибка вызова внешних сервисов: " + ex.getMessage());
    }
    return merged;
  }

  private static void mergeByPlaceId(List<PlaceDto> target, List<PlaceDto> extra) {
    Set<String> seen = new HashSet<>();
    for (PlaceDto p : target) {
      if (p.getPlaceId() != null && !p.getPlaceId().isBlank()) {
        seen.add(p.getPlaceId());
      }
    }
    for (PlaceDto p : extra) {
      String id = p.getPlaceId();
      if (id == null || id.isBlank()) {
        target.add(p);
      } else if (!seen.contains(id)) {
        seen.add(id);
        target.add(p);
      }
    }
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
    boolean hasPlaces = request.getPlaces() != null && !request.getPlaces().isEmpty();
    boolean canDiscover =
        request.getWalkLocation() != null
            && !request.getWalkLocation().isBlank()
            && !PlannerUpstreamClient.nonEmptyCategories(request.getDiscoverCategories()).isEmpty();
    if (!hasPlaces && !canDiscover) {
      throw new IllegalArgumentException("Список мест не должен быть пустым.");
    }
  }
}
