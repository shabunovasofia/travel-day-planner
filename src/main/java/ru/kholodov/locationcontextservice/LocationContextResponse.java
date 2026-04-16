package ru.kholodov.locationcontextservice;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.kholodov.locationcontextservice.enums.Pace;

import java.time.Duration;
import java.time.LocalTime;

public record LocationContextResponse(String resolvedLocation, double latitude, double longitude, int radiusMeters,
                                      double availableHours, @JsonFormat(pattern = "HH:mm") LocalTime startTime,
                                      @JsonFormat(pattern = "HH:mm") LocalTime endTime, Pace pace) {

    public static LocationContextResponse create(String resolvedLocation, double latitude,
                                                 double longitude, LocalTime startTime, LocalTime endTime, Pace pace) {
        int radius = switch (pace) {
            case SLOW -> 2000;
            case MEDIUM -> 3000;
            case FAST -> 5000;
        };

        double availableHours = Duration.between(startTime, endTime).toMinutes() / 60.0;

        return new LocationContextResponse(resolvedLocation, latitude, longitude, radius, availableHours,
                startTime, endTime, pace);
    }
}