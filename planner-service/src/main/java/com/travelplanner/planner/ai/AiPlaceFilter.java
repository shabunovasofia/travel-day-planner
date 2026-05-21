package com.travelplanner.planner.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.planner.dto.PlaceDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/** Фильтрует список мест через DeepSeek AI — выбирает лучшие для маршрута. */
@Component
public class AiPlaceFilter {

  private static final Logger logger = LoggerFactory.getLogger(AiPlaceFilter.class);
  private static final String DEEPSEEK_URL = "https://api.deepseek.com/v1/chat/completions";
  private static final int MAX_PLACES = 6;

  @Value("${deepseek.api-key:}")
  private String apiKey;

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Выбирает оптимальные места через AI.
   * Если ключ не задан или произошла ошибка — возвращает исходный список.
   *
   * @param places         все найденные места
   * @param startTime      время начала (HH:mm)
   * @param endTime        время конца (HH:mm)
   * @param availableHours доступное время в часах
   * @return отфильтрованный список мест
   */
  public List<PlaceDto> filter(
      final List<PlaceDto> places,
      final String startTime,
      final String endTime,
      final double availableHours) {

    if (apiKey == null || apiKey.isBlank()) {
      logger.info("DeepSeek ключ не задан — фильтрация AI пропущена");
      return places;
    }

    try {
      String placesJson = buildPlacesJson(places);
      String prompt = buildPrompt(placesJson, startTime, endTime, availableHours);

      Map<String, Object> requestBody = Map.of(
          "model", "deepseek-chat",
          "messages", List.of(Map.of("role", "user", "content", prompt)),
          "temperature", 0.3);

      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "Bearer " + apiKey);
      headers.set("Content-Type", "application/json");

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
      Map response = restTemplate.postForObject(DEEPSEEK_URL, entity, Map.class);

      List<String> selectedIds = parseIds(response);
      if (selectedIds.isEmpty()) {
        return places;
      }

      List<PlaceDto> filtered = places.stream()
          .filter(p -> selectedIds.contains(p.getPlaceId()))
          .collect(Collectors.toList());

      filtered = enforceFoodLimit(filtered);
      logger.info("AI выбрал {} мест из {}", filtered.size(), places.size());
      return filtered.isEmpty() ? places : filtered;

    } catch (Exception e) {
      logger.error("Ошибка AI-фильтрации, используем все места: {}", e.getMessage());
      return places;
    }
  }

  private static final int MAX_FOOD_PLACES = 2;

  private List<PlaceDto> enforceFoodLimit(final List<PlaceDto> places) {
    List<PlaceDto> result = new ArrayList<>();
    int foodCount = 0;
    for (PlaceDto p : places) {
      if (isFoodCategory(p.getCategory())) {
        if (foodCount < MAX_FOOD_PLACES) {
          result.add(p);
          foodCount++;
        } else {
          logger.info("Лимит кафе/ресторанов достигнут, пропускаем: {}", p.getName());
        }
      } else {
        result.add(p);
      }
    }
    return result;
  }

  private static boolean isFoodCategory(final String category) {
    return category != null
        && (category.equalsIgnoreCase("cafe") || category.equalsIgnoreCase("restaurant"));
  }

  private String buildPlacesJson(final List<PlaceDto> places) {
    return places.stream()
        .map(p -> String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"category\":\"%s\","
                + "\"estimatedHours\":%.1f,\"rating\":%.1f}",
            p.getPlaceId(),
            p.getName(),
            p.getCategory(),
            p.getEstimatedHours(),
            p.getRating() != null ? p.getRating() : 0.0))
        .collect(Collectors.joining(",", "[", "]"));
  }

  private String buildPrompt(
      final String placesJson,
      final String startTime,
      final String endTime,
      final double availableHours) {
    return String.format(
        "Ты помощник по планированию маршрутов. "
            + "Время прогулки: с %s до %s (%.1f ч). "
            + "Выбери как можно больше мест (но не более %d) по правилам: "
            + "1) кафе и ресторанов — не более 2 штук суммарно, остальные игнорируй; "
            + "2) музеев, парков, галерей — бери все что влезает по времени; "
            + "3) сумма estimatedHours выбранных мест не должна превышать %.1f ч; "
            + "4) рейтинг не важен — бери все подходящие места независимо от рейтинга. "
            + "Список мест: %s. "
            + "Ответь ТОЛЬКО JSON без пояснений: {\"selectedIds\": [\"id1\", \"id2\", ...]}",
        startTime, endTime, availableHours, MAX_PLACES, availableHours, placesJson);
  }

  @SuppressWarnings("unchecked")
  private List<String> parseIds(final Map<?, ?> response) throws Exception {
    List<?> choices = (List<?>) response.get("choices");
    if (choices == null || choices.isEmpty()) {
      return List.of();
    }

    Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
    String content = (String) message.get("content");

    JsonNode json = objectMapper.readTree(content);
    JsonNode ids = json.get("selectedIds");
    if (ids == null || !ids.isArray()) {
      return List.of();
    }

    return objectMapper.convertValue(ids, List.class);
  }
}
