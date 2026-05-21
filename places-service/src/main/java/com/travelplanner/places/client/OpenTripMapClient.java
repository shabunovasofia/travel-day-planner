package com.travelplanner.places.client;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public final class OpenTripMapClient {

  /** Базовый URL OpenTripMap API. */
  private static final String BASE_URL = "https://api.opentripmap.com/0.1/en/places";

  /** Базовый URL OpenStreetMap API. */
  private static final String OSM_BASE_URL = "https://api.openstreetmap.org/api/0.6";

  /** Максимальное количество мест в ответе. */
  private static final int LIMIT = 10;

  /** API-ключ OpenTripMap. */
  @Value("${opentripmap.api-key}")
  private String apiKey;

  /** HTTP-клиент для запросов к внешним API. */
  private final RestTemplate restTemplate = new RestTemplate();

  /**
   * Возвращает список мест вблизи заданных координат.
   *
   * @param lat широта
   * @param lon долгота
   * @param radius радиус поиска в метрах
   * @param kinds категория мест в формате OpenTripMap
   * @return список найденных мест
   */
  public List<OpenTripMapPlace> findPlaces(
      final double lat, final double lon, final int radius, final String kinds) {
    String url =
        String.format(
            Locale.US,
            "%s/radius?radius=%d&lon=%f&lat=%f&kinds=%s&limit=%d&apikey=%s",
            BASE_URL,
            radius,
            lon,
            lat,
            kinds,
            LIMIT,
            apiKey);

    Map response = restTemplate.getForObject(url, Map.class);
    List<Map> features = (List<Map>) response.get("features");

    if (features == null) {
      return List.of();
    }

    return features.parallelStream()
        .map(
            feature -> {
              Map properties = (Map) feature.get("properties");
              String xid = (String) properties.get("xid");
              String name = (String) properties.get("name");

              if (name == null || name.isEmpty()) {
                return null;
              }

              Map geometry = (Map) feature.get("geometry");
              List<Double> coords = (List<Double>) geometry.get("coordinates");
              double placeLon = coords.get(0);
              double placeLat = coords.get(1);

              String kindsFromResponse = (String) properties.get("kinds");
              String category = CategoryMapper.fromKinds(kindsFromResponse);

              return fetchDetails(xid, name, placeLat, placeLon, category);
            })
        .filter(place -> place != null)
        .toList();
  }

  private OpenTripMapPlace fetchDetails(
      final String xid,
      final String name,
      final double lat,
      final double lon,
      final String category) {
    try {
      String url = String.format("%s/xid/%s?apikey=%s", BASE_URL, xid, apiKey);
      Map details = restTemplate.getForObject(url, Map.class);
      String osmRef = details != null ? (String) details.get("osm") : null;

      String description = "";
      Map info = (Map) details.get("info");
      if (info != null) {
        description = (String) info.getOrDefault("descr", "");
        description = description.replaceAll("<[^>]+>", "").trim();
      }

      String address = "";
      Map addressMap = (Map) details.get("address");
      if (addressMap != null) {
        String road = (String) addressMap.getOrDefault("road", "");
        String houseNumber = (String) addressMap.getOrDefault("house_number", "");
        String city = (String) addressMap.getOrDefault("city", "");
        String suburb = (String) addressMap.getOrDefault("suburb", "");

        StringBuilder sb = new StringBuilder();
        if (!road.isEmpty()) {
          sb.append(road);
        }
        if (!houseNumber.isEmpty()) {
          sb.append(", ").append(houseNumber);
        }
        if (!suburb.isEmpty()) {
          sb.append(", ").append(suburb);
        }
        if (!city.isEmpty()) {
          sb.append(", ").append(city);
        }

        address = sb.toString();
      }

      Object rateObj = details.get("rate");
      double rating = rateObj != null ? Double.parseDouble(rateObj.toString()) : 0.0;

      String openingHoursText = null;
      boolean scheduleUnknown = true;

      if (info != null) {
        Object hours = info.get("opening_hours");
        if (hours != null) {
          openingHoursText = hours.toString();
          scheduleUnknown = false;
        }
      }

      if (openingHoursText == null || openingHoursText.isBlank()) {
        openingHoursText = loadOpeningHoursFromOsm(osmRef);
        if (openingHoursText != null && !openingHoursText.isBlank()) {
          scheduleUnknown = false;
        }
      }
      return new OpenTripMapPlace(
          xid,
          name,
          lat,
          lon,
          rating,
          address,
          description,
          openingHoursText,
          scheduleUnknown,
          category);

    } catch (Exception e) {
      return null;
    }
  }

  private String loadOpeningHoursFromOsm(final String osmRef) {
    if (osmRef == null || osmRef.isBlank()) {
      return null;
    }

    try {
      String[] parts = osmRef.split("/");
      if (parts.length != 2) {
        return null;
      }

      String type = parts[0];
      String id = parts[1];

      if (!type.equals("node") && !type.equals("way") && !type.equals("relation")) {
        return null;
      }

      String url = String.format("%s/%s/%s.json", OSM_BASE_URL, type, id);
      Map response = restTemplate.getForObject(url, Map.class);
      if (response == null) {
        return null;
      }

      List<Map> elements = (List<Map>) response.get("elements");
      if (elements == null || elements.isEmpty()) {
        return null;
      }

      Map firstElement = elements.get(0);
      Map tags = (Map) firstElement.get("tags");
      if (tags == null) {
        return null;
      }

      Object openingHours = tags.get("opening_hours");
      return openingHours != null ? openingHours.toString() : null;

    } catch (Exception e) {
      return null;
    }
  }
}
