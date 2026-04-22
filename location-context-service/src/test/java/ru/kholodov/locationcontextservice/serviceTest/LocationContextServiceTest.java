package ru.kholodov.locationcontextservice.serviceTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kholodov.locationcontextservice.dto.Coordinates;
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.LocationContextResponse;
import ru.kholodov.locationcontextservice.enums.Pace;
import ru.kholodov.locationcontextservice.exception.AddressNotFoundException;
import ru.kholodov.locationcontextservice.services.LocationContextService;
import ru.kholodov.locationcontextservice.services.GeocodingService;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationContextServiceTest {

    @Mock
    private GeocodingService geocodingService;

    @InjectMocks
    private LocationContextService service;

    @Test
    void getLocation_ShouldReturnResponseWithCorrectAddressAndTimes() {
        // given
        LocationContextRequest request = new LocationContextRequest();
        request.setLocation("Тверская, Москва");
        request.setStartTime(LocalTime.of(12, 0));
        request.setEndTime(LocalTime.of(18, 0));
        request.setPace(Pace.FAST);

        // Заглушка для геокодера: вернуть координаты Тверской (пример)
        when(geocodingService.geocode("Тверская, Москва"))
                .thenReturn(Optional.of(new Coordinates(55.757, 37.615)));

        // when
        LocationContextResponse response = service.getLocation(request);

        // then
        assertThat(response.resolvedLocation()).isEqualTo("Тверская, Москва");
        assertThat(response.latitude()).isEqualTo(55.757);
        assertThat(response.longitude()).isEqualTo(37.615);
        assertThat(response.startTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(response.endTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(response.pace()).isEqualTo(Pace.FAST);
        assertThat(response.radiusMeters()).isEqualTo(5000);
        assertThat(response.availableHours()).isEqualTo(6.0);
    }

    @Test
    void getLocation_WhenAddressNotFound_ShouldThrowException() {
        // given
        LocationContextRequest request = new LocationContextRequest();
        request.setLocation("Несуществующий адрес");
        request.setStartTime(LocalTime.NOON);
        request.setEndTime(LocalTime.NOON.plusHours(1));
        request.setPace(Pace.SLOW);

        when(geocodingService.geocode("Несуществующий адрес"))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> service.getLocation(request))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessageContaining("Не удалось найти координаты");
    }
}