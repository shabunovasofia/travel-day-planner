package com.travelplanner.planner.dto;

import java.util.List;

public record PlanResponse(
		List<PlanItem> items,
		int totalPlaces,
		double totalHours,
		List<String> warnings
) {
}
