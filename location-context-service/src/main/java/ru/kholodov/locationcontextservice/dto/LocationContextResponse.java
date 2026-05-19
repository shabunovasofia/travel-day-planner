package ru.kholodov.locationcontextservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Duration;
import java.time.LocalTime;
import ru.kholodov.locationcontextservice.enums.Pace;

public record LocationContextResponse(
    String resolvedLocation,
    double latitude,
    double longitude,
    int radiusMeters,
    double availableHours,
    @JsonFormat(pattern = "HH:mm") LocalTime startTime,
    @JsonFormat(pattern = "HH:mm") LocalTime endTime,
    Pace pace) {

  /**
   * Фабричный метод.
   *
   * @param radiusMeters уже вычисленный радиус (из изохроны или фоллбек)
   */
  public static LocationContextResponse create(
      String resolvedLocation,
      double latitude,
      double longitude,
      int radiusMeters,
      LocalTime startTime,
      LocalTime endTime,
      Pace pace) {
    double availableHours = Duration.between(startTime, endTime).toMinutes() / 60.0;
    return new LocationContextResponse(
        resolvedLocation,
        latitude,
        longitude,
        radiusMeters,
        availableHours,
        startTime,
        endTime,
        pace);
  }
}
