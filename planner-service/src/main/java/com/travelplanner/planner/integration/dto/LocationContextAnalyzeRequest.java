package com.travelplanner.planner.integration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

/** Тело запроса к {@code POST /api/v1/context/analyze} (location-context-service). */
public class LocationContextAnalyzeRequest {

  private String location;

  @JsonFormat(pattern = "HH:mm")
  private LocalTime startTime;

  @JsonFormat(pattern = "HH:mm")
  private LocalTime endTime;

  private WalkPace pace;

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  public WalkPace getPace() {
    return pace;
  }

  public void setPace(WalkPace pace) {
    this.pace = pace;
  }
}
