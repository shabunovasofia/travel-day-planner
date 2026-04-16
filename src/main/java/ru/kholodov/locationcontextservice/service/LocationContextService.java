package ru.kholodov.locationcontextservice.service;

import org.springframework.stereotype.Service;
import ru.kholodov.locationcontextservice.LocationContextRequest;
import ru.kholodov.locationcontextservice.LocationContextResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LocationContextService {
    private final Map<Long, LocationContextResponse> locationContextMap;
    private final AtomicLong idCounter;

    public LocationContextService() {
        this.locationContextMap = new HashMap<>();
        this.idCounter = new AtomicLong();
    }

    public LocationContextResponse getLocation(LocationContextRequest request) {
        // 1. Геокодинг адреса (заглушка)
        String address = request.getLocation();
        double lat = 55.7520;
        double lon = 37.5921;
        // В реальности здесь будет вызов GeocodingService

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
