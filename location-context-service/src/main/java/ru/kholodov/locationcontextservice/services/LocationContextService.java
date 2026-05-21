package ru.kholodov.locationcontextservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.kholodov.locationcontextservice.client.UpstreamServicesClient;
import ru.kholodov.locationcontextservice.dto.Coordinates;
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.places.PlacesSearchRequest;
import ru.kholodov.locationcontextservice.dto.places.PlacesSearchResponse;
import ru.kholodov.locationcontextservice.dto.planner.PlanBuildRequest;
import ru.kholodov.locationcontextservice.dto.planner.PlanBuildResponse;
import ru.kholodov.locationcontextservice.enums.Pace;
import ru.kholodov.locationcontextservice.exception.AddressNotFoundException;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Сервис оркестрации для построения готового маршрута прогулки.
 *
 * <p>Внутренне выполняет: геокодирование → изохрону → поиск мест → планирование,
 * но возвращает клиенту только финальный план посещения.
 *
 * @author Stepan Kholodov
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationContextService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final GeocodingService geocodingService;
    private final IsochroneService isochroneService;
    private final UpstreamServicesClient upstreamClient;

    /**
     * Строит готовый маршрут прогулки по заданным параметрам.
     *
     * @param request запрос с адресом, временем и темпом
     * @return готовый план с расписанием посещения мест
     * @throws AddressNotFoundException если адрес не найден
     * @throws IllegalStateException если не удалось построить план
     */
    public PlanBuildResponse.PlanData buildRoute(LocationContextRequest request) {
        // 1. Геокодирование адреса
        Coordinates coordinates = geocodingService.geocode(request.getLocation())
                .orElseThrow(() -> new AddressNotFoundException(
                        "Не удалось найти координаты для адреса: " + request.getLocation()));

        // 2. Расчёт доступного времени и радиуса
        double availableHours = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes() / 60.0;
        int radiusMeters = calculateRadius(coordinates, availableHours, request.getPace());

        // 3. Поиск мест через places-service
        var placesRequest = PlacesSearchRequest.builder()
                .latitude(coordinates.lat())
                .longitude(coordinates.lon())
                .radiusMeters(radiusMeters)
                .availableHours(availableHours)
                .build();
        List<PlacesSearchResponse.PlaceDto> places = upstreamClient.searchPlaces(placesRequest);

        if (places.isEmpty()) {
            log.warn("Не найдено мест для адреса: {}", request.getLocation());
            // Возвращаем пустой план, если мест нет
            return new PlanBuildResponse.PlanData(List.of(), 0, 0.0,
                    List.of("По заданным параметрам не найдено подходящих мест"), 0);
        }

        // 4. Построение плана через planner-service
        var planRequest = PlanBuildRequest.builder()
                .startTime(request.getStartTime().format(TIME_FORMAT))
                .endTime(request.getEndTime().format(TIME_FORMAT))
                .places(places)
                .startLatitude(coordinates.lat())
                .startLongitude(coordinates.lon())
                .build();

        PlanBuildResponse.PlanData planData = upstreamClient.buildPlan(planRequest);

        log.info("Построен маршрут: {} мест, общее время: {} ч",
                planData.getTotalPlaces(), planData.getTotalHours());

        // 5. Возврат только плана
        return planData;
    }

    /**
     * Рассчитывает радиус пешеходной доступности.
     *
     * <p>Приоритет: изохрона через OpenRouteService → fallback на линейную формулу.
     */
    private int calculateRadius(Coordinates center, double availableHours, Pace pace) {
        return isochroneService.calculateRadius(center, availableHours, pace)
                .map(d -> (int) Math.round(d))
                .orElseGet(() -> {
                    double maxDistanceKm = availableHours * pace.speedKmh / 2.0;
                    int fallback = (int) (maxDistanceKm * 1000);
                    log.warn("Изохрона недоступна. Fallback-радиус: {} м", fallback);
                    return fallback;
                });
    }
}