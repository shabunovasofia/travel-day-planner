package ru.kholodov.locationcontextservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.kholodov.locationcontextservice.dto.Coordinates;
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.LocationContextResponse;
import ru.kholodov.locationcontextservice.exception.AddressNotFoundException;

/**
 * Основной сервис для получения контекста прогулки.
 *
 * <p>Объединяет геокодирование адреса, вычисление доступного времени и
 * определение радиуса пешеходной доступности через изохронный сервис.
 * При недоступности изохрон используется fallback-расчёт на основе
 * средней скорости ходьбы (5 км/ч).
 */
@Service
public class LocationContextService {

    private static final Logger logger = LoggerFactory.getLogger(LocationContextService.class);

    private final GeocodingService geocodingService;
    private final IsochroneService isochroneService;

    private static final double DEFAULT_WALKING_SPEED_KMH = 5.0;

    public LocationContextService(
            GeocodingService geocodingService, IsochroneService isochroneService) {
        this.geocodingService = geocodingService;
        this.isochroneService = isochroneService;
    }

    /**
     * Выполняет полный анализ контекста прогулки по заданному запросу.
     *
     * <p>Шаги:
     * <ol>
     *   <li>Геокодирование адреса в координаты</li>
     *   <li>Вычисление доступного времени в часах</li>
     *   <li>Расчёт радиуса доступности через изохрону или fallback-формулу</li>
     * </ol>
     *
     * @param request запрос с адресом, временем и темпом
     * @return ответ с координатами, радиусом и временными рамками
     * @throws AddressNotFoundException если адрес не удалось геокодировать
     */
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
                                            "Изохрона недоступна. fallback-радиус: {} м", fallbackRadius);
                                    return fallbackRadius;
                                });

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
