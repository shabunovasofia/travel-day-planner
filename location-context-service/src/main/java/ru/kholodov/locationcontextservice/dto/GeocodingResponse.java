package ru.kholodov.locationcontextservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO для одного элемента ответа геокодера LocationIQ.
 *
 * <p>LocationIQ возвращает координаты как строки в JSON — поэтому {@link #lat} и {@link #lon}
 * хранятся как {@link String} и парсятся в {@code double} на стороне сервиса.
 */
@Data
public class GeocodingResponse {

  /** Широта в строковом представлении (например, {@code "55.757"}). */
  @JsonProperty("lat")
  private String lat;

  /** Долгота в строковом представлении (например, {@code "37.615"}). */
  @JsonProperty("lon")
  private String lon;

  /** Отображаемое имя найденного адреса (display_name из ответа LocationIQ). */
  @JsonProperty("display_name")
  private String displayName;
}
