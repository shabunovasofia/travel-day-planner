package com.travelplanner.places.dto;

import java.util.List;

public class PlacesSearchRequest {
  private double latitude;
  private double longitude;
  private int radiusMeters;
  private double availableHours;
  private List<String> categories; // ["museum", "park", "cafe", "gallery"]

  public PlacesSearchRequest() {
    latitude = 0.0;
    longitude = 0.0;
    radiusMeters = 0;
    availableHours = 0.0;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public int getRadiusMeters() {
    return radiusMeters;
  }

  public double getAvailableHours() {
    return availableHours;
  }

  public List<String> getCategories() {
    return categories;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public void setRadiusMeters(int radiusMeters) {
    this.radiusMeters = radiusMeters;
  }

  public void setAvailableHours(double availableHours) {
    this.availableHours = availableHours;
  }

  public void setCategories(List<String> categories) {
    this.categories = categories;
  }
}
