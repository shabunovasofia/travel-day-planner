package com.travelplanner.planner.planning;

import com.travelplanner.planner.dto.PlaceDto;
import com.travelplanner.planner.dto.PlanItem;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Подбор порядка посещения, расчёт длительности визитов и построение расписания на день.
 */
public final class RoutePlanning {

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");
	private static final int MAX_PERMUTATION_PLACES = 7;

	private RoutePlanning() {
	}

	/**
	 * Длительность визита в минутах из поля {@code estimatedHours} (округление до целых минут).
	 */
	public static long estimatedVisitMinutes(PlaceDto place) {
		double hours = place.getEstimatedHours();
		return Math.round(hours * 60.0);
	}

	public static LocalTime parseTime(String value, String field) {
		try {
			return LocalTime.parse(value.trim(), TIME_FORMAT);
		}
		catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Некорректный формат времени в поле " + field + ". Ожидается HH:mm.", e);
		}
	}

	public static String formatTime(LocalTime time) {
		return time.format(DateTimeFormatter.ofPattern("HH:mm"));
	}

	public record ScheduleOutcome(List<PlanItem> items, List<String> warnings) {
	}

	public record OptimizedSchedule(ScheduleOutcome outcome, int evaluatedOrderings) {
	}

	/**
	 * Строит расписание в заданном порядке мест (без перестановок).
	 */
	public static ScheduleOutcome scheduleOrdered(List<PlaceDto> orderedPlaces, LocalTime dayStart, LocalTime dayEnd) {
		List<PlanItem> items = new ArrayList<>();
		List<String> warnings = new ArrayList<>();
		LocalTime cursor = dayStart;
		for (PlaceDto place : orderedPlaces) {
			double hours = place.getEstimatedHours();
			if (hours <= 0) {
				warnings.add("Место пропущено: " + placeDisplayName(place) + " — некорректная длительность посещения.");
				continue;
			}
			long visitMinutes = estimatedVisitMinutes(place);
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
					formatTime(arrival),
					formatTime(departure),
					place.getCategory()
			));
			cursor = departure;
		}
		return new ScheduleOutcome(items, warnings);
	}

	/**
	 * Подбирает лучший порядок: при небольшом числе мест перебираются все перестановки и выбирается
	 * вариант с максимальным числом включённых точек, затем по сумме рейтингов включённых мест.
	 * Иначе — один жадный порядок по ранжированию (рейтинг ↓, длительность ↑, id).
	 *
	 * @return результат планирования и число проверенных упорядочений (вариантов порядка)
	 */
	public static OptimizedSchedule optimizeOrdering(List<PlaceDto> places, LocalTime dayStart, LocalTime dayEnd) {
		if (places.size() <= MAX_PERMUTATION_PLACES) {
			int n = places.size();
			int[] idx = new int[n];
			for (int i = 0; i < n; i++) {
				idx[i] = i;
			}
			ScheduleOutcome best = null;
			int bestScore = Integer.MIN_VALUE;
			int evaluated = 0;
			if (n == 0) {
				return new OptimizedSchedule(new ScheduleOutcome(List.of(), List.of()), 0);
			}
			do {
				evaluated++;
				List<PlaceDto> order = new ArrayList<>(n);
				for (int i : idx) {
					order.add(places.get(i));
				}
				ScheduleOutcome candidate = scheduleOrdered(order, dayStart, dayEnd);
				int score = scoreSchedule(candidate, places);
				if (best == null || score > bestScore) {
					bestScore = score;
					best = candidate;
				}
			} while (nextPermutation(idx));
			return new OptimizedSchedule(best, evaluated);
		}
		List<PlaceDto> ranked = new ArrayList<>(places);
		ranked.sort(RANKING_COMPARATOR);
		ScheduleOutcome outcome = scheduleOrdered(ranked, dayStart, dayEnd);
		return new OptimizedSchedule(outcome, 1);
	}

	private static int scoreSchedule(ScheduleOutcome outcome, List<PlaceDto> allPlaces) {
		double ratingSum = 0.0;
		for (PlanItem item : outcome.items()) {
			for (PlaceDto p : allPlaces) {
				if (p.getPlaceId() != null && p.getPlaceId().equals(item.placeId())) {
					if (p.getRating() != null) {
						ratingSum += p.getRating();
					}
					break;
				}
			}
		}
		return outcome.items().size() * 1_000_000 + (int) Math.round(ratingSum * 1_000.0);
	}

	private static final Comparator<PlaceDto> RANKING_COMPARATOR =
			Comparator.<PlaceDto>comparingDouble((PlaceDto p) -> p.getRating() != null ? p.getRating() : 0.0).reversed()
					.thenComparingDouble(PlaceDto::getEstimatedHours)
					.thenComparing(p -> p.getPlaceId() == null ? "" : p.getPlaceId(), String.CASE_INSENSITIVE_ORDER);

	private static boolean nextPermutation(int[] a) {
		int n = a.length;
		int k = n - 2;
		while (k >= 0 && a[k] >= a[k + 1]) {
			k--;
		}
		if (k < 0) {
			return false;
		}
		int l = n - 1;
		while (a[k] >= a[l]) {
			l--;
		}
		swap(a, k, l);
		for (int i = k + 1, j = n - 1; i < j; i++, j--) {
			swap(a, i, j);
		}
		return true;
	}

	private static void swap(int[] a, int i, int j) {
		int t = a[i];
		a[i] = a[j];
		a[j] = t;
	}

	public static double sumVisitHours(List<PlanItem> items) {
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
}
