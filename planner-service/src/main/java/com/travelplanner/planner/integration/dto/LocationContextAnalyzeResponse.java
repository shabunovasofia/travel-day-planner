package com.travelplanner.planner.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Ответ {@code /api/v1/context/analyze} — только поля, нужные для поиска мест. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationContextAnalyzeResponse {

  private DataWrapper data;

  public DataWrapper getData() {
    return data;
  }

  public void setData(DataWrapper data) {
    this.data = data;
  }

  public double getLatitude() {
    return data != null ? data.latitude : 0;
  }

  public double getLongitude() {
    return data != null ? data.longitude : 0;
  }

  public int getRadiusMeters() {
    return data != null ? data.radiusMeters : 0;
  }

  public double getAvailableHours() {
    return data != null ? data.availableHours : 0;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DataWrapper {

    private double latitude;
    private double longitude;
    private int radiusMeters;
    private double availableHours;

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
  }
}
