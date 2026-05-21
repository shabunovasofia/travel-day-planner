package ru.kholodov.locationcontextservice.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация интеграции с внешними сервисами системы.
 *
 * <p>Содержит базовые URL для вызова {@code places-service} и {@code planner-service}. Значения
 * читаются из {@code application.properties} с префиксом {@code integration}.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "integration")
public class IntegrationProperties {

  /** Базовый URL places-service. */
  @NotBlank(message = "integration.places-base-url не задан")
  private String placesBaseUrl;

  /** Базовый URL planner-service. */
  @NotBlank(message = "integration.planner-base-url не задан")
  private String plannerBaseUrl;

  /** Таймаут подключения к upstream-сервисам (мс). */
  private int connectTimeoutMs;

  /** Таймаут чтения ответа от upstream-сервисов (мс). */
  private int readTimeoutMs;
}
