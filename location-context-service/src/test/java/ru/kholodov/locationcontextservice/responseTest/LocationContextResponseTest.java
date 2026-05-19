package ru.kholodov.locationcontextservice.responseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import ru.kholodov.locationcontextservice.dto.LocationContextResponse;
import ru.kholodov.locationcontextservice.enums.Pace;

class LocationContextResponseTest {

  @Test
  void create_ShouldSetAllFieldsCorrectly() {
    LocationContextResponse response =
        LocationContextResponse.create(
            "Арбат", 55.75, 37.59, 1500, LocalTime.of(10, 0), LocalTime.of(16, 0), Pace.MEDIUM);

    assertThat(response.resolvedLocation()).isEqualTo("Арбат");
    assertThat(response.latitude()).isEqualTo(55.75);
    assertThat(response.longitude()).isEqualTo(37.59);
    assertThat(response.radiusMeters()).isEqualTo(1500);
    assertThat(response.startTime()).isEqualTo(LocalTime.of(10, 0));
    assertThat(response.endTime()).isEqualTo(LocalTime.of(16, 0));
    assertThat(response.pace()).isEqualTo(Pace.MEDIUM);
    assertThat(response.availableHours()).isCloseTo(6.0, within(0.001));
  }

  @Test
  void create_ShouldCalculateAvailableHoursCorrectly() {
    LocalTime start = LocalTime.of(9, 30);
    LocalTime end = LocalTime.of(17, 45); // 8 часов 15 минут = 8.25

    LocationContextResponse response =
        LocationContextResponse.create("Тест", 0.0, 0.0, 500, start, end, Pace.FAST);

    assertThat(response.availableHours()).isCloseTo(8.25, within(0.001));
  }
}
