package ru.kholodov.locationcontextservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO для ответа сервиса геокодирования.
 *
 * <p>Содержит широту, долготу и отображаемое имя адреса, возвращаемые
 * внешним API.
 */
@Data
public class GeocodingResponse {
    @JsonProperty("lat")
    private String lat;

    @JsonProperty("lon")
    private String lon;

    @JsonProperty("display_name")
    private String displayName;
}
