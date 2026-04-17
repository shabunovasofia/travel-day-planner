package ru.kholodov.locationcontextservice.service;

import org.springframework.stereotype.Service;
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.LocationContextResponse;


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

    public LocationContextService() {

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
        // 1. Геокодинг адреса (заглушка)
        String address = request.getLocation();
        double lat = 55.7520;
        double lon = 37.5921;

        return LocationContextResponse.create(
                address,
                lat,
                lon,
                request.getStartTime(),
                request.getEndTime(),
                request.getPace()
        );


    }
}
