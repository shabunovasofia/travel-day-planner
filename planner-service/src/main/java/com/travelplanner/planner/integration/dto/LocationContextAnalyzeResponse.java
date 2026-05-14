package com.travelplanner.planner.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Ответ {@code /api/v1/context/analyze} — только поля, нужные для поиска мест.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationContextAnalyzeResponse {

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
