package com.travelplanner.planner.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.travelplanner.planner.dto.PlaceDto;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlacesSearchApiResponse {

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
