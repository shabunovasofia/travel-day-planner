package com.travelplanner.planner.integration.dto;

import java.util.List;

/**
 * Тело запроса к {@code POST /api/v1/places/search} (places-service).
 */
public class PlacesSearchApiRequest {

	private double latitude;
	private double longitude;
	private int radiusMeters;
	private double availableHours;
	private List<String> categories;

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
