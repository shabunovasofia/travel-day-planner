package com.travelplanner.places.dto;

import java.util.List;

public class PlacesSearchResponse {

  private List<PlaceDto> places;
  private int totalFound;

  public PlacesSearchResponse(final List<PlaceDto> places, final int totalFound) {
    this.places = places;
    this.totalFound = totalFound;
  }

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
