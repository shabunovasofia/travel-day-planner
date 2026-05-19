package com.travelplanner.places.dto;

public final class PlaceDto {
  /** Идентификатор места. */
  private String placeId;

  /** Название места. */
  private String name;

  /** Категория места (museum, park, cafe и т.д.). */
  private String category;

  /** Широта. */
  private double latitude;

  /** Долгота. */
  private double longitude;

  /** Оценочное время посещения в часах. */
  private double estimatedHours;

  /** Описание места. */
  private String description;

  /** Адрес места. */
  private String address;

  /** Рейтинг места. */
  private Double rating;

  /** Текст расписания работы места. */
  private String openingHoursText;

  /** Флаг: расписание неизвестно. */
  private boolean scheduleUnknown;

  /**
   * Конструктор со всеми полями.
   *
   * @param placeId идентификатор места
   * @param name название
   * @param category категория
   * @param latitude широта
   * @param longitude долгота
   * @param estimatedHours оценочное время посещения
   * @param rating рейтинг
   * @param address адрес
   * @param description описание
   * @param openingHoursText расписание работы
   * @param scheduleUnknown флаг: расписание неизвестно
   */
  public PlaceDto(
      final String placeId,
      final String name,
      final String category,
      final double latitude,
      final double longitude,
      final double estimatedHours,
      final Double rating,
      final String address,
      final String description,
      final String openingHoursText,
      final boolean scheduleUnknown) {
    this.placeId = placeId;
    this.name = name;
    this.category = category;
    this.latitude = latitude;
    this.longitude = longitude;
    this.estimatedHours = estimatedHours;
    this.rating = rating;
    this.address = address;
    this.description = description;
    this.openingHoursText = openingHoursText;
    this.scheduleUnknown = scheduleUnknown;
  }

  /**
   * Возвращает идентификатор места.
   *
   * @return placeId
   */
  public String getPlaceId() {
    return placeId;
  }

  /**
   * Устанавливает идентификатор места.
   *
   * @param placeId идентификатор
   */
  public void setPlaceId(final String placeId) {
    this.placeId = placeId;
  }

  /**
   * Возвращает название.
   *
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Устанавливает название.
   *
   * @param name название
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Возвращает категорию.
   *
   * @return category
   */
  public String getCategory() {
    return category;
  }

  /**
   * Устанавливает категорию.
   *
   * @param category категория
   */
  public void setCategory(final String category) {
    this.category = category;
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
   * Возвращает оценочное время посещения.
   *
   * @return estimatedHours
   */
  public double getEstimatedHours() {
    return estimatedHours;
  }

  /**
   * Устанавливает оценочное время посещения.
   *
   * @param estimatedHours часы
   */
  public void setEstimatedHours(final double estimatedHours) {
    this.estimatedHours = estimatedHours;
  }

  /**
   * Возвращает описание.
   *
   * @return description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Устанавливает описание.
   *
   * @param description описание
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * Возвращает адрес.
   *
   * @return address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Устанавливает адрес.
   *
   * @param address адрес
   */
  public void setAddress(final String address) {
    this.address = address;
  }

  /**
   * Возвращает рейтинг.
   *
   * @return rating
   */
  public Double getRating() {
    return rating;
  }

  /**
   * Устанавливает рейтинг.
   *
   * @param rating рейтинг
   */
  public void setRating(final Double rating) {
    this.rating = rating;
  }

  /**
   * Возвращает расписание работы.
   *
   * @return openingHoursText
   */
  public String getOpeningHoursText() {
    return openingHoursText;
  }

  /**
   * Устанавливает расписание работы.
   *
   * @param openingHoursText расписание
   */
  public void setOpeningHoursText(final String openingHoursText) {
    this.openingHoursText = openingHoursText;
  }

  /**
   * Возвращает флаг неизвестного расписания.
   *
   * @return scheduleUnknown
   */
  public boolean isScheduleUnknown() {
    return scheduleUnknown;
  }

  /**
   * Устанавливает флаг неизвестного расписания.
   *
   * @param scheduleUnknown флаг
   */
  public void setScheduleUnknown(final boolean scheduleUnknown) {
    this.scheduleUnknown = scheduleUnknown;
  }
}
