package ru.kholodov.locationcontextservice.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурационные параметры сервиса изохрон (OpenRouteService).
 *
 * <p>Читаются из application.properties с префиксом {@code isochrone}. При отсутствии или пустом
 * значении любого из полей приложение не запустится.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "isochrone")
public class IsochroneProperties {

    /** URL эндпоинта API изохрон. */
    @NotBlank(message = "isochrone.url не задан")
    private String url;

    /** API-ключ для авторизации в OpenRouteService. */
    @NotBlank(message = "isochrone.api-key не задан (переменная окружения ISOCHRONE_API_KEY)")
    private String apiKey;
}
