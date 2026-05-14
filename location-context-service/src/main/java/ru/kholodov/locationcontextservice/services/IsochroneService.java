package ru.kholodov.locationcontextservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.kholodov.locationcontextservice.dto.Coordinates;
import ru.kholodov.locationcontextservice.enums.Pace;

@Service
public class IsochroneService {

  private static final Logger logger = LoggerFactory.getLogger(IsochroneService.class);

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${ors.url}")
  private String orsUrl;

  @Value("${ors.api-key}")
  private String orsApiKey;

  public IsochroneService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  /** Рассчитывает радиус пешеходной доступности через ORS Isochrones API (POST). */
  public Optional<Double> calculateRadius(Coordinates center, double availableHours, Pace pace) {
    double standardSpeed = 5.0; // фиксированная скорость ORS
    double paceSpeed =
        switch (pace) {
          case SLOW -> 3.5;
          case MEDIUM -> 5.0;
          case FAST -> 6.5;
        };

    long rangeSeconds = (long) (availableHours * 3600 * paceSpeed / standardSpeed);
    if (rangeSeconds < 60) {
      logger.warn("Слишком маленькое скорректированное время: {} секунд", rangeSeconds);
      return Optional.empty();
    }

    try {
      Map<String, Object> body =
          Map.of(
              "locations", List.of(List.of(center.lon(), center.lat())),
              "range", List.of(rangeSeconds));

      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", orsApiKey);
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(List.of(MediaType.valueOf("application/geo+json")));

      HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
      logger.debug("Запрос изохроны (POST): {}", orsUrl);

      ResponseEntity<JsonNode> response =
          restTemplate.exchange(orsUrl, HttpMethod.POST, entity, JsonNode.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return extractAverageDistance(response.getBody(), center);
      } else {
        logger.warn("ORS вернул статус: {}", response.getStatusCode());
      }
    } catch (Exception e) {
      logger.error("Ошибка при вызове ORS Isochrones API: {}", e.getMessage(), e);
    }
    return Optional.empty();
  }

  /** Извлекает максимальное расстояние (по прямой) от центра до любой точки изохроны. */
  private Optional<Double> extractAverageDistance(JsonNode geoJson, Coordinates center) {
    try {
      JsonNode features = geoJson.get("features");
      if (features == null || !features.isArray() || features.size() == 0) {
        logger.warn("В ответе нет фич изохрон");
        return Optional.empty();
      }

      double totalDistance = 0;
      int pointCount = 0;

      for (JsonNode feature : features) {
        JsonNode geometry = feature.get("geometry");
        if (geometry == null) continue;
        JsonNode coordinates = geometry.get("coordinates");
        if (coordinates == null) continue;

        String type = geometry.get("type").asText();
        if ("Polygon".equals(type)) {
          // Внешнее кольцо — массив точек [lon, lat]
          JsonNode outerRing = coordinates.get(0);
          if (outerRing != null) {
            for (JsonNode point : outerRing) {
              double lon = point.get(0).asDouble();
              double lat = point.get(1).asDouble();
              totalDistance += haversineDistance(center.lat(), center.lon(), lat, lon);
              pointCount++;
            }
          }
        } else if ("MultiPolygon".equals(type)) {
          for (JsonNode polygon : coordinates) {
            JsonNode outerRing = polygon.get(0);
            if (outerRing != null) {
              for (JsonNode point : outerRing) {
                double lon = point.get(0).asDouble();
                double lat = point.get(1).asDouble();
                totalDistance += haversineDistance(center.lat(), center.lon(), lat, lon);
                pointCount++;
              }
            }
          }
        }
      }

      if (pointCount > 0) {
        double avg = totalDistance / pointCount;
        logger.info(
            "Среднее расстояние изохроны: {} м (по {} точкам)", Math.round(avg), pointCount);
        return Optional.of(avg);
      }
    } catch (Exception e) {
      logger.error("Ошибка разбора GeoJSON изохроны: {}", e.getMessage(), e);
    }
    return Optional.empty();
  }

  /** Формула гаверсинусов (результат в метрах). */
  private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
    final double R = 6371000; // радиус Земли в метрах
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }
}
