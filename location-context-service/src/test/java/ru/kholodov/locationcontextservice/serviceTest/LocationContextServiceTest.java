package ru.kholodov.locationcontextservice.serviceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kholodov.locationcontextservice.client.UpstreamServicesClient;
import ru.kholodov.locationcontextservice.dto.Coordinates;
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.places.PlacesSearchRequest;
import ru.kholodov.locationcontextservice.dto.places.PlacesSearchResponse;
import ru.kholodov.locationcontextservice.dto.planner.PlanBuildRequest;
import ru.kholodov.locationcontextservice.dto.planner.PlanBuildResponse;
import ru.kholodov.locationcontextservice.enums.Pace;
import ru.kholodov.locationcontextservice.exception.AddressNotFoundException;
import ru.kholodov.locationcontextservice.services.GeocodingService;
import ru.kholodov.locationcontextservice.services.IsochroneService;
import ru.kholodov.locationcontextservice.services.LocationContextService;

/**
 * Тесты сервиса {@link LocationContextService}.
 *
 * <p>Проверяют оркестрацию вызовов: геокодирование → изохрона → поиск мест → планирование.
 */
@ExtendWith(MockitoExtension.class)
class LocationContextServiceTest {

  @Mock private GeocodingService geocodingService;

  @Mock private IsochroneService isochroneService;

  @Mock private UpstreamServicesClient upstreamClient;

  @InjectMocks private LocationContextService service;

  @Test
  @DisplayName("buildRoute — успешное построение маршрута с местами")
  void buildRoute_ShouldReturnPlanData_WhenPlacesFound() {
    // given
    LocationContextRequest request = new LocationContextRequest();
    request.setLocation("Арбат, Москва");
    request.setStartTime(LocalTime.of(10, 0));
    request.setEndTime(LocalTime.of(16, 0));
    request.setPace(Pace.MEDIUM);

    // Мок геокодирования
    Coordinates coords = new Coordinates(55.751199, 37.5898715);
    when(geocodingService.geocode("Арбат, Москва")).thenReturn(Optional.of(coords));

    // Мок изохроны
    when(isochroneService.calculateRadius(eq(coords), eq(6.0), eq(Pace.MEDIUM)))
        .thenReturn(Optional.of(3000.0));

    // Мок поиска мест
    PlacesSearchResponse.PlaceDto place1 =
        new PlacesSearchResponse.PlaceDto(
            "osm_museum_123",
            "Пушкинский музей",
            "museum",
            55.7520,
            37.5921,
            2.5,
            "описание",
            4.8,
            "ул. Волхонка, 12",
            "10:00-18:00",
            false);
    PlacesSearchResponse.PlaceDto place2 =
        new PlacesSearchResponse.PlaceDto(
            "osm_cafe_456",
            "Кафе «Уголёк»",
            "cafe",
            55.7530,
            37.5930,
            1.0,
            "вкусно",
            4.5,
            "ул. Арбат, 10",
            "09:00-22:00",
            false);

    when(upstreamClient.searchPlaces(any(PlacesSearchRequest.class)))
        .thenReturn(List.of(place1, place2));

    // Мок планирования
    PlanBuildResponse.PlanItem planItem1 =
        new PlanBuildResponse.PlanItem(
            "osm_museum_123", "Пушкинский музей", "10:15", "12:45", "museum", 15);
    PlanBuildResponse.PlanItem planItem2 =
        new PlanBuildResponse.PlanItem(
            "osm_cafe_456", "Кафе «Уголёк»", "13:00", "14:00", "cafe", 10);

    PlanBuildResponse.PlanData expectedPlan =
        new PlanBuildResponse.PlanData(List.of(planItem1, planItem2), 2, 3.5, List.of(), 24);

    when(upstreamClient.buildPlan(any(PlanBuildRequest.class))).thenReturn(expectedPlan);

    // when
    PlanBuildResponse.PlanData result = service.buildRoute(request);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTotalPlaces()).isEqualTo(2);
    assertThat(result.getTotalHours()).isEqualTo(3.5);
    assertThat(result.getItems()).hasSize(2);
    assertThat(result.getItems().get(0).getPlaceName()).isEqualTo("Пушкинский музей");
    assertThat(result.getItems().get(1).getPlaceName()).isEqualTo("Кафе «Уголёк»");

    // Проверка вызовов
    verify(geocodingService).geocode("Арбат, Москва");
    verify(isochroneService).calculateRadius(eq(coords), eq(6.0), eq(Pace.MEDIUM));
    verify(upstreamClient).searchPlaces(any(PlacesSearchRequest.class));
    verify(upstreamClient).buildPlan(any(PlanBuildRequest.class));
  }

  @Test
  @DisplayName("buildRoute — возврат пустого плана, если мест не найдено")
  void buildRoute_ShouldReturnEmptyPlan_WhenNoPlacesFound() {
    // given
    LocationContextRequest request = new LocationContextRequest();
    request.setLocation("Пустырь, Москва");
    request.setStartTime(LocalTime.of(10, 0));
    request.setEndTime(LocalTime.of(12, 0));
    request.setPace(Pace.SLOW);

    when(geocodingService.geocode("Пустырь, Москва"))
        .thenReturn(Optional.of(new Coordinates(55.8, 37.6)));
    when(isochroneService.calculateRadius(any(), eq(2.0), eq(Pace.SLOW)))
        .thenReturn(Optional.of(1500.0));
    when(upstreamClient.searchPlaces(any(PlacesSearchRequest.class)))
        .thenReturn(List.of()); // пустой список

    // when
    PlanBuildResponse.PlanData result = service.buildRoute(request);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTotalPlaces()).isZero();
    assertThat(result.getItems()).isEmpty();
    assertThat(result.getWarnings()).hasSize(1);
    assertThat(result.getWarnings().get(0)).contains("не найдено подходящих мест");
  }

  @Test
  @DisplayName("buildRoute — выбрасывает AddressNotFoundException, если адрес не найден")
  void buildRoute_ShouldThrowException_WhenAddressNotFound() {
    // given
    LocationContextRequest request = new LocationContextRequest();
    request.setLocation("Несуществующий адрес"); // ← исправлено: был "Несуществщий"
    request.setStartTime(LocalTime.NOON);
    request.setEndTime(LocalTime.NOON.plusHours(1));
    request.setPace(Pace.SLOW);

    when(geocodingService.geocode("Несуществующий адрес")).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> service.buildRoute(request))
        .isInstanceOf(AddressNotFoundException.class)
        .hasMessageContaining("Не удалось найти координаты для адреса: Несуществующий адрес");
  }

  @Test
  @DisplayName("buildRoute — используется fallback-радиус, если изохрона недоступна")
  void buildRoute_ShouldUseFallbackRadius_WhenIsochroneFails() {
    // given
    LocationContextRequest request = new LocationContextRequest();
    request.setLocation("Кремль, Москва");
    request.setStartTime(LocalTime.of(9, 0));
    request.setEndTime(LocalTime.of(12, 0)); // 3 часа
    request.setPace(Pace.FAST); // 6.5 км/ч

    Coordinates coords = new Coordinates(55.7520, 37.6175);
    when(geocodingService.geocode("Кремль, Москва")).thenReturn(Optional.of(coords));

    // Изохрона возвращает пустой Optional → используется fallback
    when(isochroneService.calculateRadius(eq(coords), eq(3.0), eq(Pace.FAST)))
        .thenReturn(Optional.empty());

    when(upstreamClient.searchPlaces(any(PlacesSearchRequest.class))).thenReturn(List.of());

    // when
    service.buildRoute(request);

    // then
    // Fallback-формула: (3.0 * 6.5 / 2.0) * 1000 = 9750 метров
    // Проверяем, что searchPlaces был вызван с радиусом ~9750
    verify(upstreamClient)
        .searchPlaces(
            argThat(
                req ->
                    req.getRadiusMeters() == 9750
                        && req.getLatitude() == 55.7520
                        && req.getLongitude() == 37.6175));
  }

  /** Helper-метод для аргумент-матчеров в verify(). */
  private static <T> T argThat(java.util.function.Predicate<T> predicate) {
    return org.mockito.ArgumentMatchers.argThat(predicate::test);
  }
}
