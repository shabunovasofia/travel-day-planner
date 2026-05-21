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

  /** Обратное соответствие: kinds OpenTripMap → наша категория. */
  private static final Map<String, String> KINDS_TO_CATEGORY =
      Map.of(
          "museums", "museum",
          "art_galleries", "gallery",
          "nature_reserves", "park",
          "cafes", "cafe",
          "restaurants", "restaurant");

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

  /**
   * Определяет нашу категорию по строке kinds из ответа OpenTripMap.
   *
   * @param kinds строка вида "museums,cultural" из ответа API
   * @return категория или null если не определена
   */
  public static String fromKinds(final String kinds) {
    if (kinds == null || kinds.isBlank()) {
      return null;
    }
    for (String kind : kinds.split(",")) {
      String category = KINDS_TO_CATEGORY.get(kind.trim());
      if (category != null) {
        return category;
      }
    }
    return null;
  }
}
