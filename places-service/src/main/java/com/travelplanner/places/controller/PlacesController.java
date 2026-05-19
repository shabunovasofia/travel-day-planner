package com.travelplanner.places.controller;

import com.travelplanner.places.dto.PlacesSearchRequest;
import com.travelplanner.places.dto.PlacesSearchResponse;
import com.travelplanner.places.service.PlacesService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/places")
public class PlacesController {
  /** Сервис для поиска мест. */
  private final PlacesService placesService;

  /**
   * Создаёт контроллер с заданным сервисом.
   *
   * @param service сервис поиска мест
   */
  public PlacesController(final PlacesService service) {
    this.placesService = service;
  }

  /**
   * Выполняет поиск мест по координатам и категориям.
   *
   * @param request параметры поиска
   * @return список найденных мест
   */
  @PostMapping("/search")
  public ResponseEntity<Map<String, Object>> searchResponse(
      @Valid @RequestBody final PlacesSearchRequest request) {
    PlacesSearchResponse response = placesService.search(request);
    return ResponseEntity.ok(Map.of("data", response));
  }
}
