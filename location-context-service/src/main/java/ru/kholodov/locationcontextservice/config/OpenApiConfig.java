package ru.kholodov.locationcontextservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация springdoc-openapi.
 *
 * <p>Задаёт метаданные API (название, описание, версия, контактные данные), которые отображаются в
 * Swagger UI ({@code /swagger-ui.html}) и в JSON-схеме ({@code /v3/api-docs}).
 *
 * @author Stepan Kholodov
 */
@Configuration
public class OpenApiConfig {

  /**
   * Бин с описанием API для springdoc.
   *
   * @return объект {@link OpenAPI} с метаданными сервиса
   */
  @Bean
  public OpenAPI locationContextOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Location Context Service API")
                .description(
                    "Сервис-оркестратор: геокодирует адрес, рассчитывает изохрону "
                        + "пешеходной доступности, ищет места и строит расписание прогулки.")
                .version("0.0.1")
                .contact(new Contact().name("Stepan Kholodov").email("kholodov_stepan@inbox.ru")));
  }
}
