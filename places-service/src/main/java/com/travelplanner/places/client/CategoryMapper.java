package com.travelplanner.places.client;

import java.util.Map;

public class CategoryMapper {
  private static final Map<String, String> CATEGORY_TO_KINDS =
      Map.of(
          "museum", "museums",
          "gallery", "art_galleries",
          "park", "nature_reserves",
          "cafe", "cafes",
          "restaurant", "restaurants");

  public static String toKinds(String category) {
    return CATEGORY_TO_KINDS.getOrDefault(category, "interesting_places");
  }
}
