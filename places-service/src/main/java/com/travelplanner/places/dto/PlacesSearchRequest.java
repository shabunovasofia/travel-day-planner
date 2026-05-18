package com.travelplanner.places.dto;

import java.util.List;

public class PlacesSearchRequest {
  private double latitude;
  private double longitude;
  private int radiusMeters;
  private double availableHours;
  private List<String> categories;

  public PlacesSearchRequest() {
    latitude = 0.0;
    longitude = 0.0;
    radiusMeters = 0;
    availableHours = 0.0;
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

  public int getRadiusMeters() {
    return radiusMeters;
  }

  public void setRadiusMeters(int radiusMeters) {
    this.radiusMeters = radiusMeters;
  }

  public double getAvailableHours() {
    return availableHours;
  }

  public void setAvailableHours(double availableHours) {
    this.availableHours = availableHours;
  }

  public List<String> getCategories() {
    return categories;
  }

  public void setCategories(List<String> categories) {
    this.categories = categories;
  }
}
