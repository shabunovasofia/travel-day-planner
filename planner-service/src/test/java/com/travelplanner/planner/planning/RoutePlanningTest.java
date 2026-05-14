package com.travelplanner.planner.planning;

import com.travelplanner.planner.dto.PlaceDto;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoutePlanningTest {

	@Test
	void estimatedVisitMinutes_roundsHoursToMinutes() {
		PlaceDto p = place("a", "A", 1.25, 4.0);
		assertThat(RoutePlanning.estimatedVisitMinutes(p)).isEqualTo(75L);
	}

	@Test
	void optimizeOrdering_countsAllPermutationsForSmallInput() {
		List<PlaceDto> places = List.of(
				place("a", "A", 1.0, 5.0),
				place("b", "B", 1.0, 4.0));
		RoutePlanning.OptimizedSchedule result =
				RoutePlanning.optimizeOrdering(places, LocalTime.of(10, 0), LocalTime.of(12, 0));
		assertThat(result.evaluatedOrderings()).isEqualTo(2);
		assertThat(result.outcome().items()).hasSize(2);
	}

	@Test
	void optimizeOrdering_prefersHigherRatedPlaceWhenOnlyOneFits() {
		PlaceDto high = place("h", "High", 2.0, 5.0);
		PlaceDto low = place("l", "Low", 2.0, 3.0);
		List<PlaceDto> places = List.of(low, high);
		RoutePlanning.OptimizedSchedule result =
				RoutePlanning.optimizeOrdering(places, LocalTime.of(10, 0), LocalTime.of(13, 0));
		assertThat(result.outcome().items()).hasSize(1);
		assertThat(result.outcome().items().get(0).placeId()).isEqualTo("h");
	}

	@Test
	void optimizeOrdering_evaluatesFactorialForThreePlaces() {
		List<PlaceDto> places = List.of(
				place("1", "P1", 0.5, 4.0),
				place("2", "P2", 0.5, 4.0),
				place("3", "P3", 0.5, 4.0));
		RoutePlanning.OptimizedSchedule result =
				RoutePlanning.optimizeOrdering(places, LocalTime.of(9, 0), LocalTime.of(12, 0));
		assertThat(result.evaluatedOrderings()).isEqualTo(6);
		assertThat(result.outcome().items()).hasSize(3);
	}

	@Test
	void optimizeOrdering_usesSingleGreedyOrderingForLargeInput() {
		List<PlaceDto> places = List.of(
				place("low-long", "Low long", 1.0, 3.0),
				place("top-short", "Top short", 0.5, 5.0),
				place("mid", "Mid", 0.5, 4.0),
				place("a", "A", 0.5, 3.5),
				place("b", "B", 0.5, 3.4),
				place("c", "C", 0.5, 3.3),
				place("d", "D", 0.5, 3.2),
				place("e", "E", 0.5, 3.1));

		RoutePlanning.OptimizedSchedule result =
				RoutePlanning.optimizeOrdering(places, LocalTime.of(10, 0), LocalTime.of(14, 0));

		assertThat(result.evaluatedOrderings()).isEqualTo(1);
		assertThat(result.outcome().items()).extracting("placeId")
				.startsWith("top-short", "mid");
	}

	@Test
	void scheduleOrdered_warnsAboutInvalidActivityDuration() {
		PlaceDto invalid = place("bad", "Bad", 0.0, 4.0);

		RoutePlanning.ScheduleOutcome result =
				RoutePlanning.scheduleOrdered(List.of(invalid), LocalTime.of(10, 0), LocalTime.of(12, 0));

		assertThat(result.items()).isEmpty();
		assertThat(result.warnings()).isNotEmpty();
	}

	private static PlaceDto place(String id, String name, double hours, double rating) {
		PlaceDto p = new PlaceDto();
		p.setPlaceId(id);
		p.setName(name);
		p.setCategory("park");
		p.setLatitude(55.0);
		p.setLongitude(37.0);
		p.setEstimatedHours(hours);
		p.setDescription("");
		p.setRating(rating);
		return p;
	}
}
