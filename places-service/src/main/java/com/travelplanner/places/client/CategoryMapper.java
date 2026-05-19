package com.travelplanner.places.client;

import java.util.Map;

public final class CategoryMapper {

  /** Соответствие категорий мест тегам OpenTripMap API. */
  private static final Map<String, String> CATEGORY_TO_KINDS =
      Map.of(
          "museum", "museums",
          "gallery", "art_galleries",
          "park", "nature_reserves",
          "cafe", "cafes",
          "restaurant", "restaurants");

  private CategoryMapper() {}

  /**
   * Возвращает тег OpenTripMap для заданной категории.
   *
   * @param category категория места (museum, park, cafe и т.д.)
   * @return строка с тегом для запроса к API
   */
  public static String toKinds(final String category) {
    return CATEGORY_TO_KINDS.getOrDefault(category, "interesting_places");
  }
}
