package com.travelplanner.places.service;

import com.travelplanner.places.client.CategoryMapper;
import com.travelplanner.places.client.OpenTripMapClient;
import com.travelplanner.places.client.OpenTripMapPlace;
import com.travelplanner.places.dto.PlaceDto;
import com.travelplanner.places.dto.PlacesSearchRequest;
import com.travelplanner.places.dto.PlacesSearchResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PlacesService {
  private final OpenTripMapClient openTripMapClient;

  public PlacesService(OpenTripMapClient openTripMapClient) {
    this.openTripMapClient = openTripMapClient;
  }

  public PlacesSearchResponse search(PlacesSearchRequest request) {
    List<String> categories = request.getCategories();
    if (categories == null || categories.isEmpty()) {
      categories = List.of("museum", "park", "cafe");
    }
    List<PlaceDto> result = new ArrayList<>();
    double hoursLeft = request.getAvailableHours();
    for (String category : categories) {
      String osmFilter = CategoryMapper.toKinds(category);
      double hours = estimatedHoursByCategory(category);
      List<OpenTripMapPlace> places =
          openTripMapClient.findPlaces(
              request.getLatitude(), request.getLongitude(), request.getRadiusMeters(), osmFilter);
      for (OpenTripMapPlace place : places) {
        if (hoursLeft < hours) break;
        result.add(
            new PlaceDto(
                "osm_" + category + "_" + result.size(),
                place.name(),
                category,
                place.latitude(),
                place.longitude(),
                hours,
                place.rating(),
                place.address(),
                place.description()));
        hoursLeft -= hours;
      }
    }

    return new PlacesSearchResponse(result, result.size());
  }

  private double estimatedHoursByCategory(String category) {
    return switch (category) {
      case "museum", "gallery" -> 2.5;
      case "park" -> 1.5;
      case "cafe", "restaurant" -> 1.0;
      default -> 1.5;
    };
  }
}
