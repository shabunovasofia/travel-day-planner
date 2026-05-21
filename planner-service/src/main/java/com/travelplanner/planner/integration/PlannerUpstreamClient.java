package com.travelplanner.planner.integration;

import com.travelplanner.planner.dto.PlaceDto;
import com.travelplanner.planner.integration.dto.LocationContextAnalyzeRequest;
import com.travelplanner.planner.integration.dto.LocationContextAnalyzeResponse;
import com.travelplanner.planner.integration.dto.PlacesSearchApiRequest;
import com.travelplanner.planner.integration.dto.PlacesSearchApiResponse;
import com.travelplanner.planner.integration.dto.WalkPace;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** HTTP-вызовы location-context-service и places-service при сборке плана (ЛР3). */
@Component
@ConditionalOnProperty(
    prefix = "planner.integration",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class PlannerUpstreamClient {

  private final RestClient locationContextClient;
  private final RestClient placesClient;

  public PlannerUpstreamClient(PlannerIntegrationProperties properties) {
    this.locationContextClient =
        RestClient.builder().baseUrl(trimSlash(properties.getLocationContextBaseUrl())).build();
    this.placesClient =
        RestClient.builder().baseUrl(trimSlash(properties.getPlacesBaseUrl())).build();
  }

  public LocationContextAnalyzeResponse analyzeLocation(
      String location, LocalTime startTime, LocalTime endTime, WalkPace pace)
      throws RestClientException {
    LocationContextAnalyzeRequest body = new LocationContextAnalyzeRequest();
    body.setLocation(location);
    body.setStartTime(startTime);
    body.setEndTime(endTime);
    body.setPace(pace);
    return locationContextClient
        .post()
        .uri("/api/v1/context/analyze")
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(LocationContextAnalyzeResponse.class);
  }

  public List<PlaceDto> searchPlaces(
      double latitude,
      double longitude,
      int radiusMeters,
      double availableHours,
      List<String> categories)
      throws RestClientException {
    PlacesSearchApiRequest body = new PlacesSearchApiRequest();
    body.setLatitude(latitude);
    body.setLongitude(longitude);
    body.setRadiusMeters(radiusMeters);
    body.setAvailableHours(availableHours);
    body.setCategories(categories);
    PlacesSearchApiResponse response =
        placesClient
            .post()
            .uri("/api/v1/places/search")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(PlacesSearchApiResponse.class);
    if (response == null || response.getData() == null || response.getData().getPlaces() == null) {
      return List.of();
    }
    return response.getData().getPlaces();
  }

  private static String trimSlash(String url) {
    if (url == null || url.isBlank()) {
      return "http://localhost:8081";
    }
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }

  public static WalkPace parseWalkPace(String raw) {
    if (raw == null || raw.isBlank()) {
      return WalkPace.MEDIUM;
    }
    try {
      return WalkPace.valueOf(raw.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Некорректный темп прогулки: ожидается SLOW, MEDIUM или FAST.", e);
    }
  }

  public static List<String> nonEmptyCategories(List<String> categories) {
    if (categories == null || categories.isEmpty()) {
      return Collections.emptyList();
    }
    return categories.stream().filter(c -> c != null && !c.isBlank()).map(String::trim).toList();
  }
}
