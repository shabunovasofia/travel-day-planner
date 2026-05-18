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
  private static final List<String> DEFAULT_CATEGORIES = List.of("museum", "park", "cafe");

  private final OpenTripMapClient openTripMapClient;

  public PlacesService(final OpenTripMapClient openTripMapClient) {
    this.openTripMapClient = openTripMapClient;
  }

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
        PlaceDto dto = mapToPlaceDto(place, category, estimatedHours);

        if (isSuitableForAvailableTime(dto, request.getAvailableHours())) {
          candidates.add(dto);
        }
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

  private boolean isSuitableForAvailableTime(final PlaceDto place, final double availableHours) {
    return place.getEstimatedHours() <= availableHours;
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
      case "museum", "gallery" -> 2.5;
      case "park" -> 1.5;
      case "cafe", "restaurant" -> 1.0;
      default -> 1.5;
    };
  }
}
