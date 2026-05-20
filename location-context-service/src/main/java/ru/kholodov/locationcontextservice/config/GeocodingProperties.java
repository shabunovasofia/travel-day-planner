package ru.kholodov.locationcontextservice.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурационные параметры сервиса геокодирования (LocationIQ).
 *
 * <p>Читаются из application.properties с префиксом {@code geocoding}. При отсутствии или пустом
 * значении любого из полей приложение не запустится.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "geocoding")
public class GeocodingProperties {

    /** URL эндпоинта API геокодирования. */
    @NotBlank(message = "geocoding.url не задан")
    private String url;

    /** API-ключ для авторизации в LocationIQ. */
    @NotBlank(message = "geocoding.api-key не задан (переменная окружения GEOCODING_API_KEY)")
    private String apiKey;
}
