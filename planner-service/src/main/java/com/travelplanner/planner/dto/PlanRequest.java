package com.travelplanner.planner.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanRequest {

  private String startTime;
  private String endTime;
  private List<PlaceDto> places;

  /** Адрес для вызова location-context-service (опционально, ЛР3). */
  private String walkLocation;

  /** SLOW, MEDIUM, FAST — по умолчанию MEDIUM. */
  private String walkPace;

  /** Категории для поиска в places-service после анализа контекста. */
  private List<String> discoverCategories;

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

  public String getWalkLocation() {
    return walkLocation;
  }

  public void setWalkLocation(String walkLocation) {
    this.walkLocation = walkLocation;
  }

  public String getWalkPace() {
    return walkPace;
  }

  public void setWalkPace(String walkPace) {
    this.walkPace = walkPace;
  }

  public List<String> getDiscoverCategories() {
    return discoverCategories;
  }

  public void setDiscoverCategories(List<String> discoverCategories) {
    this.discoverCategories = discoverCategories;
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
