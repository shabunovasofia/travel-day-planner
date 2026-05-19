package com.travelplanner.places;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlacesServiceApplication {
  /**
   * Запускает приложение.
   *
   * @param args аргументы командной строки
   */
  public static void main(final String[] args) {
    SpringApplication.run(PlacesServiceApplication.class, args);
  }
}
