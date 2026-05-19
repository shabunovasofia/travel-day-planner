package com.travelplanner.places.service;

import com.travelplanner.places.client.CategoryMapper;
import com.travelplanner.places.client.OpenTripMapClient;
import com.travelplanner.places.client.OpenTripMapPlace;
import com.travelplanner.places.dto.PlaceDto;
import com.travelplanner.places.dto.PlacesSearchRequest;
import com.travelplanner.places.dto.PlacesSearchResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PlacesService {
  /** Категории по умолчанию, если в запросе не указаны. */
  private static final List<String> DEFAULT_CATEGORIES = List.of("museum", "park", "cafe");

  /** Оценочное время посещения музея или галереи в часах. */
  private static final double HOURS_MUSEUM = 2.5;

  /** Оценочное время посещения парка в часах. */
  private static final double HOURS_PARK = 1.5;

  /** Оценочное время посещения кафе или ресторана в часах. */
  private static final double HOURS_CAFE = 1.0;

  /** Оценочное время посещения по умолчанию в часах. */
  private static final double HOURS_DEFAULT = 1.5;

  /** Клиент OpenTripMap API. */
  private final OpenTripMapClient openTripMapClient;

  /**
   * Конструктор с зависимостью.
   *
   * @param openTripMapClient клиент OpenTripMap
   */
  public PlacesService(final OpenTripMapClient openTripMapClient) {
    this.openTripMapClient = openTripMapClient;
  }

  /**
   * Ищет места по параметрам запроса.
   *
   * @param request параметры поиска
   * @return список найденных мест
   */
  public PlacesSearchResponse search(final PlacesSearchRequest request) {
    List<String> categories = resolveCategories(request.getCategories());
    List<PlaceDto> candidates = new ArrayList<>();

    for (String category : categories) {
      String osmFilter = CategoryMapper.toKinds(category);
      double estimatedHours = estimatedHoursByCategory(category);

      List<OpenTripMapPlace> foundPlaces =
          openTripMapClient.findPlaces(
              request.getLatitude(), request.getLongitude(), request.getRadiusMeters(), osmFilter);

      for (OpenTripMapPlace place : foundPlaces) {
        candidates.add(mapToPlaceDto(place, category, estimatedHours));
      }
    }

    List<PlaceDto> uniqueCandidates = removeDuplicates(candidates);
    uniqueCandidates.sort(byRatingDesc());

    return new PlacesSearchResponse(uniqueCandidates, uniqueCandidates.size());
  }

  private List<String> resolveCategories(final List<String> categories) {
    if (categories == null || categories.isEmpty()) {
      return DEFAULT_CATEGORIES;
    }
    return categories;
  }

  private PlaceDto mapToPlaceDto(
      final OpenTripMapPlace place, final String category, final double estimatedHours) {
    return new PlaceDto(
        buildPlaceId(place, category),
        place.name(),
        category,
        place.latitude(),
        place.longitude(),
        estimatedHours,
        place.rating(),
        place.address(),
        place.description(),
        place.openingHoursText(),
        place.scheduleUnknown());
  }

  private String buildPlaceId(final OpenTripMapPlace place, final String category) {
    return "osm_" + category + "_" + place.xid();
  }

  private List<PlaceDto> removeDuplicates(final List<PlaceDto> places) {
    Map<String, PlaceDto> unique = new LinkedHashMap<>();

    for (PlaceDto place : places) {
      unique.putIfAbsent(place.getPlaceId(), place);
    }

    return new ArrayList<>(unique.values());
  }

  private Comparator<PlaceDto> byRatingDesc() {

    return Comparator.comparing(
        PlaceDto::getRating, Comparator.nullsLast(Comparator.reverseOrder()));
  }

  private double estimatedHoursByCategory(final String category) {
    return switch (category) {
      case "museum", "gallery" -> HOURS_MUSEUM;
      case "park" -> HOURS_PARK;
      case "cafe", "restaurant" -> HOURS_CAFE;
      default -> HOURS_DEFAULT;
    };
  }
}
