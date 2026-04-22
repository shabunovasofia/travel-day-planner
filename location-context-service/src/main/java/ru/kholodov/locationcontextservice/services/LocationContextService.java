package ru.kholodov.locationcontextservice.services;

import org.springframework.stereotype.Service;
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.LocationContextResponse;
import ru.kholodov.locationcontextservice.dto.Coordinates;
import ru.kholodov.locationcontextservice.exception.AddressNotFoundException;

/**
 * Сервис для обработки контекста локации.
 * <p>
 * Отвечает за получение координат по текстовому адресу (пока заглушка)
 * и формирование итогового ответа с помощью фабричного метода.
 * </p>
 *
 */
@Service
public class LocationContextService {
    private final GeocodingService geocodingService;

    public LocationContextService(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }



    /**
     * Обрабатывает запрос и формирует ответ с контекстом прогулки.
     * <p>
     * На текущем этапе геокодирование эмулируется константными координатами
     * для адреса "Арбат, Москва". В будущем будет заменено на вызов внешнего API.
     * </p>
     *
     * @param request объект запроса с адресом, временем и темпом
     * @return объект {@link LocationContextResponse} с вычисленными данными
     */
    public LocationContextResponse getLocation(LocationContextRequest request) {
        String address = request.getLocation();

        // 1. Получаем координаты от внешнего API
        Coordinates coordinates = geocodingService.geocode(address)
                .orElseThrow(() -> new AddressNotFoundException("Не удалось найти координаты для адреса: " + address));

        // 2. Формируем ответ с реальными координатами
        return LocationContextResponse.create(
                address,
                coordinates.lat(),
                coordinates.lon(),
                request.getStartTime(),
                request.getEndTime(),
                request.getPace()
        );
    }
}
