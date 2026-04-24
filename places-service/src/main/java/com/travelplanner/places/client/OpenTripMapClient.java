package com.travelplanner.places.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OpenTripMapClient {
  private static final String BASE_URL = "https://api.opentripmap.com/0.1/en/places";
  private static final String API_KEY = "5ae2e3f221c38a28845f05b6cb8ed279c51bf2d1a4a2a453d7693f05";
  private static final int LIMIT = 10;

  private final RestTemplate restTemplate = new RestTemplate();

  public List<OpenTripMapPlace> findPlaces(double lat, double lon, int radius, String kinds) {
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
            API_KEY);
    Map response = restTemplate.getForObject(url, Map.class);
    List<Map> features = (List<Map>) response.get("features");

    if (features == null) return List.of();

    List<OpenTripMapPlace> result = new ArrayList<>();
    for (Map feature : features) {
      Map properties = (Map) feature.get("properties");
      String xid = (String) properties.get("xid");
      String name = (String) properties.get("name");

      if (name == null || name.isEmpty()) continue;

      Map geometry = (Map) feature.get("geometry");
      List<Double> coords = (List<Double>) geometry.get("coordinates");
      double placeLon = coords.get(0);
      double placeLat = coords.get(1);

      OpenTripMapPlace place = fetchDetails(xid, name, placeLat, placeLon);
      if (place != null) result.add(place);
    }
    return result;
  }

  private OpenTripMapPlace fetchDetails(String xid, String name, double lat, double lon) {
    try {
      String url = String.format("%s/xid/%s?apikey=%s", BASE_URL, xid, API_KEY);
      Map details = restTemplate.getForObject(url, Map.class);

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
        if (!road.isEmpty()) sb.append(road);
        if (!houseNumber.isEmpty()) sb.append(", ").append(houseNumber);
        if (!suburb.isEmpty()) sb.append(", ").append(suburb);
        if (!city.isEmpty()) sb.append(", ").append(city);

        address = sb.toString();
      }

      Object rateObj = details.get("rate");
      double rating = rateObj != null ? Double.parseDouble(rateObj.toString()) : 0.0;

      return new OpenTripMapPlace(xid, name, lat, lon, rating, address, description);

    } catch (Exception e) {
      return null;
    }
  }
}
