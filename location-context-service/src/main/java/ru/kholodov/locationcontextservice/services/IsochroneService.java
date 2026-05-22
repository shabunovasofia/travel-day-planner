package ru.kholodov.locationcontextservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.kholodov.locationcontextservice.config.IsochroneProperties;
import ru.kholodov.locationcontextservice.dto.Coordinates;
import ru.kholodov.locationcontextservice.enums.Pace;

/**
 * Сервис расчёта радиуса пешеходной доступности через OpenRouteService Isochrones API.
 *
 * <p>Инициализирует {@link RestClient} один раз с baseUrl и заголовком авторизации. При каждом
 * вызове {@link #calculateRadius} передаются только изменяемые параметры: координаты, время, темп.
 * Результатом является среднее расстояние (в метрах) от центра до точек изохроны, рассчитанное по
 * формуле гаверсинусов.
 *
 * <p>Транзиентные ошибки (5xx, таймауты сети) обрабатываются Resilience4j. Бизнес-ошибки (4xx) и
 * ошибки разбора ответа не ретраятся — возвращается {@link Optional#empty()}.
 *
 * <p>Результаты кэшируются через Spring Cache (Caffeine) по ключу {@code lat,lon:hours:pace} с TTL
 * 1 час.
 */
@Slf4j
@Service
public class IsochroneService {

  private static final double STANDARD_SPEED_KMH = 5.0;
  private static final double EARTH_RADIUS_M = 6_371_000.0;
  private static final String RESILIENCE_NAME = "isochrone";

  private final RestClient restClient;

  /**
   * @param builder общий {@link RestClient.Builder}
   * @param props параметры доступа к ORS Isochrones (URL + API-ключ)
   */
  public IsochroneService(RestClient.Builder builder, IsochroneProperties props) {
    this.restClient =
        builder.baseUrl(props.getUrl()).defaultHeader("Authorization", props.getApiKey()).build();
  }

  /**
   * Рассчитывает радиус пешеходной доступности (изохрону) для заданного центра, доступного времени
   * и темпа.
   *
   * <p>Скорость берётся из {@link Pace#speedKmh}. Доступное время конвертируется в секунды для API
   * ORS.
   *
   * @param center координаты центра
   * @param availableHours доступное время прогулки в часах
   * @param pace темп прогулки
   * @return Optional со средним радиусом в метрах, либо пустой Optional при ошибке
   */
  @Cacheable(
      value = "isochrone",
      key = "#center.lat() + ',' + #center.lon() + ':' + #availableHours + ':' + #pace")
  @Retry(name = RESILIENCE_NAME)
  @CircuitBreaker(name = RESILIENCE_NAME, fallbackMethod = "calculateRadiusFallback")
  public Optional<Double> calculateRadius(Coordinates center, double availableHours, Pace pace) {
    long rangeSeconds = (long) (availableHours * 3600 * pace.getSpeedKmh() / STANDARD_SPEED_KMH);
    if (rangeSeconds < 60) {
      log.warn("Слишком маленькое скорректированное время: {} секунд", rangeSeconds);
      return Optional.empty();
    }

    JsonNode response;
    try {
      Map<String, Object> body =
          Map.of(
              "locations", List.of(List.of(center.lon(), center.lat())),
              "range", List.of(rangeSeconds));

      log.debug("Запрос изохроны (POST) для координат: {},{}", center.lat(), center.lon());

      response =
          restClient
              .post()
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.valueOf("application/geo+json"))
              .body(body)
              .retrieve()
              .body(JsonNode.class);
    } catch (HttpClientErrorException e) {
      log.warn("ORS Isochrones 4xx {}: запрос не будет ретраиться", e.getStatusCode());
      return Optional.empty();
    }

    if (response == null) {
      return Optional.empty();
    }
    return extractAverageDistance(response, center);
  }

  @SuppressWarnings("unused")
  private Optional<Double> calculateRadiusFallback(
      Coordinates center, double availableHours, Pace pace, Throwable t) {
    log.error("Isochrone fallback (CB open / retries exhausted): {}", t.toString());
    return Optional.empty();
  }

  /**
   * Вычисляет среднее расстояние от центра до точек изохроны по GeoJSON-ответу.
   *
   * <p>Поддерживает геометрии {@code Polygon} и {@code MultiPolygon}. Расстояние считается формулой
   * гаверсинусов от центра до каждой вершины внешнего кольца, затем усредняется.
   *
   * @param geoJson корневой узел GeoJSON-ответа ORS
   * @param center координаты центра, от которого считается расстояние
   * @return Optional со средним расстоянием в метрах, либо пустой при ошибке разбора
   */
  private Optional<Double> extractAverageDistance(JsonNode geoJson, Coordinates center) {
    try {
      JsonNode features = geoJson.get("features");
      if (features == null || !features.isArray() || features.isEmpty()) {
        log.warn("В ответе нет фич изохрон");
        return Optional.empty();
      }

      double totalDistance = 0;
      int pointCount = 0;

      for (JsonNode feature : features) {
        JsonNode geometry = feature.get("geometry");
        if (geometry == null) {
          continue;
        }
        JsonNode coordinates = geometry.get("coordinates");
        if (coordinates == null) {
          continue;
        }

        String type = geometry.get("type").asText();
        if ("Polygon".equals(type)) {
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
        log.info("Среднее расстояние изохроны: {} м (по {} точкам)", Math.round(avg), pointCount);
        return Optional.of(avg);
      }
    } catch (Exception e) {
      log.error("Ошибка разбора GeoJSON изохроны: {}", e.getMessage(), e);
    }
    return Optional.empty();
  }

  /**
   * Формула гаверсинусов: расстояние между двумя точками на сфере Земли.
   *
   * @param lat1 широта первой точки
   * @param lon1 долгота первой точки
   * @param lat2 широта второй точки
   * @param lon2 долгота второй точки
   * @return расстояние в метрах
   */
  private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_M * c;
  }
}
