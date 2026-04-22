package ru.kholodov.locationcontextservice.responseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import ru.kholodov.locationcontextservice.dto.LocationContextResponse;
import ru.kholodov.locationcontextservice.enums.Pace;

class LocationContextResponseTest {

  @Test
  void create_ShouldCalculateRadiusForSlowPace() {
    LocationContextResponse response =
        LocationContextResponse.create(
            "Арбат", 55.75, 37.59, LocalTime.of(10, 0), LocalTime.of(16, 0), Pace.SLOW);

    assertThat(response.radiusMeters()).isEqualTo(2000);
  }

  @Test
  void create_ShouldCalculateRadiusForMediumPace() {
    LocationContextResponse response =
        LocationContextResponse.create(
            "Арбат", 55.75, 37.59, LocalTime.of(10, 0), LocalTime.of(16, 0), Pace.MEDIUM);

    assertThat(response.radiusMeters()).isEqualTo(3000);
  }

  @Test
  void create_ShouldCalculateRadiusForFastPace() {
    LocationContextResponse response =
        LocationContextResponse.create(
            "Арбат", 55.75, 37.59, LocalTime.of(10, 0), LocalTime.of(16, 0), Pace.FAST);

    assertThat(response.radiusMeters()).isEqualTo(5000);
  }

  @Test
  void create_ShouldCalculateAvailableHoursCorrectly() {
    LocalTime start = LocalTime.of(9, 30);
    LocalTime end = LocalTime.of(17, 45); // 8 часов 15 минут = 8.25 часа

    LocationContextResponse response =
        LocationContextResponse.create("Тест", 0.0, 0.0, start, end, Pace.MEDIUM);

    assertThat(response.availableHours()).isCloseTo(8.25, within(0.001));
  }
}
