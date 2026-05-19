package ru.kholodov.locationcontextservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.kholodov.locationcontextservice.dto.Coordinates;
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.LocationContextResponse;
import ru.kholodov.locationcontextservice.exception.AddressNotFoundException;

@Service
public class LocationContextService {

  private static final Logger logger = LoggerFactory.getLogger(LocationContextService.class);

  private final GeocodingService geocodingService;
  private final IsochroneService isochroneService;

  // Скорость для fallback (км/ч)
  private static final double DEFAULT_WALKING_SPEED_KMH = 5.0;

  public LocationContextService(
      GeocodingService geocodingService, IsochroneService isochroneService) {
    this.geocodingService = geocodingService;
    this.isochroneService = isochroneService;
  }

  public LocationContextResponse getLocation(LocationContextRequest request) {
    String address = request.getLocation();

    // 1. Геокодирование
    Coordinates coordinates =
        geocodingService
            .geocode(address)
            .orElseThrow(
                () ->
                    new AddressNotFoundException(
                        "Не удалось найти координаты для адреса: " + address));

    // 2. Вычисляем радиус пешеходной доступности
    double availableHours =
        java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes() / 60.0;

    int radiusMeters =
        isochroneService
            .calculateRadius(coordinates, availableHours, request.getPace())
            .map(d -> (int) Math.round(d))
            .orElseGet(
                () -> {
                  // Fallback: радиус = (доступное время * скорость) / 2 (туда‑обратно)
                  double maxDistanceKm = availableHours * DEFAULT_WALKING_SPEED_KMH / 2.0;
                  int fallbackRadius = (int) (maxDistanceKm * 1000);
                  logger.warn(
                      "Изохрона недоступна. Использую fallback-радиус: {} м", fallbackRadius);
                  return fallbackRadius;
                });

    // 3. Формируем ответ
    return LocationContextResponse.create(
        address,
        coordinates.lat(),
        coordinates.lon(),
        radiusMeters,
        request.getStartTime(),
        request.getEndTime(),
        request.getPace());
  }
}
