package com.travelplanner.places.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;

public class PlacesSearchRequest {
  /** Широта центра поиска. */
  private double latitude;

  /** Долгота центра поиска. */
  private double longitude;

  /** Радиус поиска в метрах. */
  @Min(value = 1, message = "radiusMeters must be positive")
  private int radiusMeters;

  /** Доступное время в часах. */
  @Positive(message = "availableHours must be positive")
  private double availableHours;

  /** Список категорий мест для поиска. */
  private List<String> categories;

  /** Конструктор по умолчанию. */
  public PlacesSearchRequest() {
    latitude = 0.0;
    longitude = 0.0;
    radiusMeters = 0;
    availableHours = 0.0;
  }

  /**
   * Возвращает широту.
   *
   * @return latitude
   */
  public double getLatitude() {
    return latitude;
  }

  /**
   * Устанавливает широту.
   *
   * @param latitude широта
   */
  public void setLatitude(final double latitude) {
    this.latitude = latitude;
  }

  /**
   * Возвращает долготу.
   *
   * @return longitude
   */
  public double getLongitude() {
    return longitude;
  }

  /**
   * Устанавливает долготу.
   *
   * @param longitude долгота
   */
  public void setLongitude(final double longitude) {
    this.longitude = longitude;
  }

  /**
   * Возвращает радиус поиска.
   *
   * @return radiusMeters
   */
  public int getRadiusMeters() {
    return radiusMeters;
  }

  /**
   * Устанавливает радиус поиска.
   *
   * @param radiusMeters радиус в метрах
   */
  public void setRadiusMeters(final int radiusMeters) {
    this.radiusMeters = radiusMeters;
  }

  /**
   * Возвращает доступное время.
   *
   * @return availableHours
   */
  public double getAvailableHours() {
    return availableHours;
  }

  /**
   * Устанавливает доступное время.
   *
   * @param availableHours часы
   */
  public void setAvailableHours(final double availableHours) {
    this.availableHours = availableHours;
  }

  /**
   * Возвращает список категорий.
   *
   * @return categories
   */
  public List<String> getCategories() {
    return categories;
  }

  /**
   * Устанавливает список категорий.
   *
   * @param categories категории
   */
  public void setCategories(final List<String> categories) {
    this.categories = categories;
  }
}
