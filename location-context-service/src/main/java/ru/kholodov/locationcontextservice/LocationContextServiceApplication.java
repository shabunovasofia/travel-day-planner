package ru.kholodov.locationcontextservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Точка входа Spring Boot-приложения {@code location-context-service}.
 *
 * <p>Сервис-оркестратор для построения маршрута прогулки: геокодирует адрес, считает изохрону
 * пешеходной доступности, обращается к {@code places-service} и {@code planner-service}, возвращает
 * готовый план.
 *
 * <p>Включает Spring Cache (Caffeine) и сканирование классов-конфигов {@link
 * org.springframework.boot.context.properties.ConfigurationProperties}.
 *
 * @author Stepan Kholodov
 */
@SpringBootApplication
@EnableCaching
@ConfigurationPropertiesScan
public class LocationContextServiceApplication {

  /**
   * Запускает Spring Boot-приложение.
   *
   * @param args аргументы командной строки
   */
  public static void main(String[] args) {
    SpringApplication.run(LocationContextServiceApplication.class, args);
  }
}
