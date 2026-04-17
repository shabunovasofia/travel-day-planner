package com.travelplanner.planner.service;

import com.travelplanner.planner.dto.PlaceDto;
import com.travelplanner.planner.dto.PlanItem;
import com.travelplanner.planner.dto.PlanRequest;
import com.travelplanner.planner.dto.PlanResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlannerService {

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

	public PlanResponse build(PlanRequest request) {
		validateRequest(request);
		LocalTime dayStart = parseTime(request.getStartTime(), "startTime");
		LocalTime dayEnd = parseTime(request.getEndTime(), "endTime");
		if (!dayStart.isBefore(dayEnd)) {
			throw new IllegalArgumentException("startTime должно быть раньше endTime.");
		}

		List<PlanItem> items = new ArrayList<>();
		List<String> warnings = new ArrayList<>();
		LocalTime cursor = dayStart;

		for (PlaceDto place : request.getPlaces()) {
			double hours = place.getEstimatedHours();
			if (hours <= 0) {
				warnings.add("Место пропущено: " + placeDisplayName(place) + " — некорректная длительность посещения.");
				continue;
			}
			long visitMinutes = Math.round(hours * 60.0);
			if (visitMinutes <= 0) {
				warnings.add("Место пропущено: " + placeDisplayName(place) + " — некорректная длительность посещения.");
				continue;
			}
			LocalTime arrival = cursor;
			LocalTime departure = arrival.plusMinutes(visitMinutes);
			if (departure.isAfter(dayEnd)) {
				warnings.add("Не удалось включить в план: " + placeDisplayName(place) + " (" + place.getPlaceId()
						+ ") — недостаточно времени в доступном интервале дня.");
				continue;
			}
			items.add(new PlanItem(
					place.getPlaceId(),
					place.getName(),
					format(arrival),
					format(departure),
					place.getCategory()
			));
			cursor = departure;
		}

		double totalHours = sumVisitHours(items);

		return new PlanResponse(items, items.size(), totalHours, warnings);
	}

	private void validateRequest(PlanRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Тело запроса не задано.");
		}
		if (request.getPlaces() == null || request.getPlaces().isEmpty()) {
			throw new IllegalArgumentException("Список мест не должен быть пустым.");
		}
		if (request.getStartTime() == null || request.getStartTime().isBlank()) {
			throw new IllegalArgumentException("Поле startTime обязательно.");
		}
		if (request.getEndTime() == null || request.getEndTime().isBlank()) {
			throw new IllegalArgumentException("Поле endTime обязательно.");
		}
	}

	private static double sumVisitHours(List<PlanItem> items) {
		double sum = 0.0;
		for (PlanItem item : items) {
			LocalTime a = parseTime(item.arrivalTime(), "arrivalTime");
			LocalTime d = parseTime(item.departureTime(), "departureTime");
			sum += Duration.between(a, d).toMinutes() / 60.0;
		}
		return sum;
	}

	private static String placeDisplayName(PlaceDto place) {
		if (place.getName() != null && !place.getName().isBlank()) {
			return place.getName();
		}
		return place.getPlaceId() != null ? place.getPlaceId() : "без названия";
	}

	private static LocalTime parseTime(String value, String field) {
		try {
			return LocalTime.parse(value.trim(), TIME_FORMAT);
		}
		catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Некорректный формат времени в поле " + field + ". Ожидается HH:mm.", e);
		}
	}

	private static String format(LocalTime time) {
		return time.format(DateTimeFormatter.ofPattern("HH:mm"));
	}
}
