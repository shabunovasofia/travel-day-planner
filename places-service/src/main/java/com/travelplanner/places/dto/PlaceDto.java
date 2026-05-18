package com.travelplanner.places.dto;

public class PlaceDto {

  private String placeId;
  private String name;
  private String category;
  private double latitude;
  private double longitude;
  private double estimatedHours;
  private String description;
  private String address;
  private Double rating;
  private String openingHoursText;
  private boolean scheduleUnknown;

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

  public String getPlaceId() {
    return placeId;
  }

  public void setPlaceId(String placeId) {
    this.placeId = placeId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getEstimatedHours() {
    return estimatedHours;
  }

  public void setEstimatedHours(double estimatedHours) {
    this.estimatedHours = estimatedHours;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Double getRating() {
    return rating;
  }

  public void setRating(Double rating) {
    this.rating = rating;
  }

  public String getOpeningHoursText() {
    return openingHoursText;
  }

  public void setOpeningHoursText(String openingHoursText) {
    this.openingHoursText = openingHoursText;
  }

  public boolean isScheduleUnknown() {
    return scheduleUnknown;
  }

  public void setScheduleUnknown(boolean scheduleUnknown) {
    this.scheduleUnknown = scheduleUnknown;
  }
}
