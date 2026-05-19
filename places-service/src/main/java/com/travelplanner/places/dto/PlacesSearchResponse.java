package com.travelplanner.places.dto;

import java.util.List;

public class PlacesSearchResponse {

  /** Список найденных мест. */
  private List<PlaceDto> places;

  /** Общее количество найденных мест. */
  private int totalFound;

  /**
   * Конструктор с полями.
   *
   * @param places список мест
   * @param totalFound количество найденных мест
   */
  public PlacesSearchResponse(final List<PlaceDto> places, final int totalFound) {
    this.places = places;
    this.totalFound = totalFound;
  }

  /**
   * Возвращает список мест.
   *
   * @return places
   */
  public List<PlaceDto> getPlaces() {
    return places;
  }

  /**
   * Устанавливает список мест.
   *
   * @param places список мест
   */
  public void setPlaces(final List<PlaceDto> places) {
    this.places = places;
  }

  /**
   * Возвращает количество найденных мест.
   *
   * @return totalFound
   */
  public int getTotalFound() {
    return totalFound;
  }

  /**
   * Устанавливает количество найденных мест.
   *
   * @param totalFound количество
   */
  public void setTotalFound(final int totalFound) {
    this.totalFound = totalFound;
  }
}
