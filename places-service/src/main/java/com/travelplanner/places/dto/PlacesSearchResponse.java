package com.travelplanner.places.dto;

import java.util.List;

public class PlacesSearchResponse {

    private List<PlaceDto> places;     // List<PlaceDto> — список найденных мест
    private int totalFound; // int — сколько мест найдено

    public PlacesSearchResponse(List<PlaceDto> places, int totalFound) {
        this.places = places;
        this.totalFound = totalFound;
    }

    public int getTotalFound() {
        return totalFound;
    }

    public List<PlaceDto> getPlaces() {
        return places;
    }

    public void setPlaces(List<PlaceDto> places) {
        this.places = places;
    }

    public void setTotalFound(int totalFound) {
        this.totalFound = totalFound;
    }
}

