package ru.kholodov.locationcontextservice.service;

import org.springframework.stereotype.Service;
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.LocationContextResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LocationContextService {

    public LocationContextService() {

    }

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
