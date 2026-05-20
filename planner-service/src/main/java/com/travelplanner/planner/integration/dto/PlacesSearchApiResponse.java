package com.travelplanner.planner.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.travelplanner.planner.dto.PlaceDto;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlacesSearchApiResponse {

  private DataWrapper data;

  public DataWrapper getData() {
    return data;
  }

  public void setData(DataWrapper data) {
    this.data = data;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DataWrapper {

    private List<PlaceDto> places;
    private int totalFound;

    public List<PlaceDto> getPlaces() {
      return places;
    }

    public void setPlaces(List<PlaceDto> places) {
      this.places = places;
    }

    public int getTotalFound() {
      return totalFound;
    }

    public void setTotalFound(int totalFound) {
      this.totalFound = totalFound;
    }
  }
}
