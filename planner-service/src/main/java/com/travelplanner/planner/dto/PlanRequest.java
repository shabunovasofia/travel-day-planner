package com.travelplanner.planner.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanRequest {

  private String startTime;
  private String endTime;
  private List<PlaceDto> places;
  private Double startLatitude;
  private Double startLongitude;

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public List<PlaceDto> getPlaces() {
    return places;
  }

  public void setPlaces(List<PlaceDto> places) {
    this.places = places;
  }

  public Double getStartLatitude() {
    return startLatitude;
  }

  public void setStartLatitude(Double startLatitude) {
    this.startLatitude = startLatitude;
  }

  public Double getStartLongitude() {
    return startLongitude;
  }

  public void setStartLongitude(Double startLongitude) {
    this.startLongitude = startLongitude;
  }
}
