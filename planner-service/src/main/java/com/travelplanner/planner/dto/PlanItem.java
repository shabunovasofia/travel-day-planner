package com.travelplanner.planner.dto;

public record PlanItem(
		String placeId,
		String placeName,
		String arrivalTime,
		String departureTime,
		String category
) {
}
