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

/** Подбор порядка посещения, расчёт длительности визитов и построение расписания на день. */
public final class RoutePlanning {

  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");
  private static final int MAX_PERMUTATION_PLACES = 7;
  private static final double WALKING_SPEED_KMH = 5.0;
  private static final double EARTH_RADIUS_KM = 6371.0;

  private RoutePlanning() {}

  /** Длительность визита в минутах из поля {@code estimatedHours} (округление до целых минут). */
  public static long estimatedVisitMinutes(PlaceDto place) {
    double hours = place.getEstimatedHours();
    return Math.round(hours * 60.0);
  }

  public static LocalTime parseTime(String value, String field) {
    try {
      return LocalTime.parse(value.trim(), TIME_FORMAT);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Некорректный формат времени в поле " + field + ". Ожидается HH:mm.", e);
    }
  }

  public static String formatTime(LocalTime time) {
    return time.format(DateTimeFormatter.ofPattern("HH:mm"));
  }

  public record ScheduleOutcome(List<PlanItem> items, List<String> warnings) {}

  public record OptimizedSchedule(ScheduleOutcome outcome, int evaluatedOrderings) {}

  /** Строит расписание в заданном порядке мест (без перестановок). */
  public static ScheduleOutcome scheduleOrdered(
      List<PlaceDto> orderedPlaces, LocalTime dayStart, LocalTime dayEnd) {
    return scheduleOrdered(orderedPlaces, dayStart, dayEnd, null, null);
  }

  /** Строит расписание в заданном порядке мест с учётом пешего перехода от стартовой точки. */
  public static ScheduleOutcome scheduleOrdered(
      List<PlaceDto> orderedPlaces,
      LocalTime dayStart,
      LocalTime dayEnd,
      Double startLat,
      Double startLon) {
    List<PlanItem> items = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    LocalTime cursor = dayStart;

    double prevLat = startLat != null ? startLat : Double.NaN;
    double prevLon = startLon != null ? startLon : Double.NaN;

    for (PlaceDto place : orderedPlaces) {
      double hours = place.getEstimatedHours();
      if (hours <= 0) {
        warnings.add(
            "Место пропущено: "
                + placeDisplayName(place)
                + " — некорректная длительность посещения.");
        continue;
      }
      long visitMinutes = estimatedVisitMinutes(place);
      if (visitMinutes <= 0) {
        warnings.add(
            "Место пропущено: "
                + placeDisplayName(place)
                + " — некорректная длительность посещения.");
        continue;
      }

      int travelMinutes = 0;
      if (!Double.isNaN(prevLat) && !Double.isNaN(prevLon)) {
        travelMinutes = walkingMinutes(prevLat, prevLon, place.getLatitude(), place.getLongitude());
      }
      LocalTime arrival = cursor.plusMinutes(travelMinutes);

      if (place.isScheduleUnknown()) {
        warnings.add(
            "«"
                + placeDisplayName(place)
                + "» — расписание неизвестно, включено предположительно.");
      } else if (place.getOpeningHoursText() != null) {
        ScheduleCheckResult check = checkSchedule(place.getOpeningHoursText(), arrival);
        if (check == ScheduleCheckResult.CLOSED) {
          warnings.add(
              "Место пропущено: «"
                  + placeDisplayName(place)
                  + "» — закрыто в "
                  + formatTime(arrival)
                  + ".");
          continue;
        }
      }

      LocalTime departure = arrival.plusMinutes(visitMinutes);
      if (departure.isAfter(dayEnd)) {
        warnings.add(
            "Не удалось включить в план: "
                + placeDisplayName(place)
                + " ("
                + place.getPlaceId()
                + ") — недостаточно времени в доступном интервале дня.");
        continue;
      }
      items.add(
          new PlanItem(
              place.getPlaceId(),
              place.getName(),
              formatTime(arrival),
              formatTime(departure),
              place.getCategory(),
              travelMinutes));
      cursor = departure;
      prevLat = place.getLatitude();
      prevLon = place.getLongitude();
    }
    return new ScheduleOutcome(items, warnings);
  }

  /**
   * Подбирает лучший порядок: при небольшом числе мест перебираются все перестановки и выбирается
   * вариант с максимальным числом включённых точек, затем по сумме рейтингов включённых мест. Иначе
   * — один жадный порядок по ранжированию (рейтинг ↓, длительность ↑, id).
   *
   * @return результат планирования и число проверенных упорядочений (вариантов порядка)
   */
  public static OptimizedSchedule optimizeOrdering(
      List<PlaceDto> places, LocalTime dayStart, LocalTime dayEnd) {
    return optimizeOrdering(places, dayStart, dayEnd, null, null);
  }

  public static OptimizedSchedule optimizeOrdering(
      List<PlaceDto> places,
      LocalTime dayStart,
      LocalTime dayEnd,
      Double startLat,
      Double startLon) {
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
        ScheduleOutcome candidate = scheduleOrdered(order, dayStart, dayEnd, startLat, startLon);
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
    ScheduleOutcome outcome = scheduleOrdered(ranked, dayStart, dayEnd, startLat, startLon);
    return new OptimizedSchedule(outcome, 1);
  }

  private static int scoreSchedule(ScheduleOutcome outcome, List<PlaceDto> allPlaces) {
    double ratingSum = 0.0;
    int consecutiveFoodPenalty = 0;
    String prevCategory = null;
    for (PlanItem item : outcome.items()) {
      String cat = item.category();
      if (isFoodCategory(cat) && isFoodCategory(prevCategory)) {
        consecutiveFoodPenalty += 500_000;
      }
      prevCategory = cat;
      for (PlaceDto p : allPlaces) {
        if (p.getPlaceId() != null && p.getPlaceId().equals(item.placeId())) {
          if (p.getRating() != null) {
            ratingSum += p.getRating();
          }
          break;
        }
      }
    }
    return outcome.items().size() * 1_000_000
        + (int) Math.round(ratingSum * 1_000.0)
        - consecutiveFoodPenalty;
  }

  private static boolean isFoodCategory(String category) {
    return category != null
        && (category.equalsIgnoreCase("cafe") || category.equalsIgnoreCase("restaurant"));
  }

  private static final Comparator<PlaceDto> RANKING_COMPARATOR =
      Comparator.<PlaceDto>comparingDouble(
              (PlaceDto p) -> p.getRating() != null ? p.getRating() : 0.0)
          .reversed()
          .thenComparingDouble(PlaceDto::getEstimatedHours)
          .thenComparing(
              p -> p.getPlaceId() == null ? "" : p.getPlaceId(), String.CASE_INSENSITIVE_ORDER);

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

  /** Расстояние между двумя точками по формуле Haversine в километрах. */
  public static double haversine(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  /** Время пешего перехода между двумя точками в минутах при скорости 5 км/ч. */
  public static int walkingMinutes(double lat1, double lon1, double lat2, double lon2) {
    double distKm = haversine(lat1, lon1, lat2, lon2);
    return (int) Math.ceil(distKm / WALKING_SPEED_KMH * 60);
  }

  private enum ScheduleCheckResult {
    OPEN,
    CLOSED,
    UNKNOWN
  }

  private static ScheduleCheckResult checkSchedule(String openingHoursText, LocalTime arrival) {
    if (openingHoursText == null || openingHoursText.isBlank()) {
      return ScheduleCheckResult.UNKNOWN;
    }
    String text = openingHoursText.trim();
    if (text.equalsIgnoreCase("24/7")) {
      return ScheduleCheckResult.OPEN;
    }
    java.util.regex.Matcher matcher =
        java.util.regex.Pattern.compile("(\\d{1,2}:\\d{2})-(\\d{1,2}:\\d{2})").matcher(text);
    if (matcher.find()) {
      try {
        LocalTime open = LocalTime.parse(matcher.group(1), TIME_FORMAT);
        LocalTime close = LocalTime.parse(matcher.group(2), TIME_FORMAT);
        if (arrival.isBefore(open) || !arrival.isBefore(close)) {
          return ScheduleCheckResult.CLOSED;
        }
        return ScheduleCheckResult.OPEN;
      } catch (DateTimeParseException e) {
        return ScheduleCheckResult.UNKNOWN;
      }
    }
    return ScheduleCheckResult.UNKNOWN;
  }
}
