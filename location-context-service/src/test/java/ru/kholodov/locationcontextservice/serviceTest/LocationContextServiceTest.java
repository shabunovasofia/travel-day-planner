package ru.kholodov.locationcontextservice.serviceTest;


import org.junit.jupiter.api.Test;

import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.LocationContextResponse;
import ru.kholodov.locationcontextservice.enums.Pace;
import ru.kholodov.locationcontextservice.service.LocationContextService;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class LocationContextServiceTest {

    private final LocationContextService service = new LocationContextService();

    @Test
    void getLocation_ShouldReturnResponseWithCorrectAddressAndTimes() {
        LocationContextRequest request = new LocationContextRequest();
        request.setLocation("Тверская, Москва");
        request.setStartTime(LocalTime.of(12, 0));
        request.setEndTime(LocalTime.of(18, 0));
        request.setPace(Pace.FAST);

        LocationContextResponse response = service.getLocation(request);

        assertThat(response.resolvedLocation()).isEqualTo("Тверская, Москва");
        assertThat(response.startTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(response.endTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(response.pace()).isEqualTo(Pace.FAST);
        assertThat(response.radiusMeters()).isEqualTo(5000);
        assertThat(response.availableHours()).isEqualTo(6.0);
    }

    @Test
    void getLocation_ShouldReturnHardcodedCoordinatesForNow() {
        LocationContextRequest request = new LocationContextRequest();
        request.setLocation("Любой адрес");
        request.setStartTime(LocalTime.NOON);
        request.setEndTime(LocalTime.NOON.plusHours(1));
        request.setPace(Pace.SLOW);

        LocationContextResponse response = service.getLocation(request);

        // Заглушка всегда возвращает координаты Арбата
        assertThat(response.latitude()).isEqualTo(55.7520);
        assertThat(response.longitude()).isEqualTo(37.5921);
    }
}