package ru.kholodov.locationcontextservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.kholodov.locationcontextservice.dto.Coordinates;
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.LocationContextResponse;
import ru.kholodov.locationcontextservice.exception.AddressNotFoundException;

/**
 * Основной сервис для получения контекста прогулки.
 *
 * <p>Объединяет геокодирование адреса, вычисление доступного времени и определение радиуса
 * пешеходной доступности через изохронный сервис. При недоступности изохрон используется
 * fallback-расчёт на основе скорости текущего темпа ({@link ru.kholodov.locationcontextservice.enums.Pace#speedKmh}).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationContextService {

    private final GeocodingService geocodingService;
    private final IsochroneService isochroneService;

    /**
     * Выполняет полный анализ контекста прогулки по заданному запросу.
     *
     * <p>Шаги:
     *
     * <ol>
     *   <li>Геокодирование адреса в координаты
     *   <li>Вычисление доступного времени в часах
     *   <li>Расчёт радиуса доступности через изохрону или fallback-формулу
     * </ol>
     *
     * @param request запрос с адресом, временем и темпом
     * @return ответ с координатами, радиусом и временными рамками
     * @throws AddressNotFoundException если адрес не удалось геокодировать
     */
    public LocationContextResponse getLocation(LocationContextRequest request) {
        String address = request.getLocation();

        Coordinates coordinates =
                geocodingService
                        .geocode(address)
                        .orElseThrow(
                                () ->
                                        new AddressNotFoundException(
                                                "Не удалось найти координаты для адреса: "
                                                        + address));

        double availableHours =
                java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes()
                        / 60.0;

        int radiusMeters =
                isochroneService
                        .calculateRadius(coordinates, availableHours, request.getPace())
                        .map(d -> (int) Math.round(d))
                        .orElseGet(
                                () -> {
                                    double maxDistanceKm =
                                            availableHours * request.getPace().speedKmh / 2.0;
                                    int fallbackRadius = (int) (maxDistanceKm * 1000);
                                    log.warn(
                                            "Изохрона недоступна. Темп: {}, fallback-радиус: {} м",
                                            request.getPace(),
                                            fallbackRadius);
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
