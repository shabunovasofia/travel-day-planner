package ru.kholodov.locationcontextservice.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.kholodov.locationcontextservice.controller.LocationContextController;
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.planner.PlanBuildResponse;
import ru.kholodov.locationcontextservice.enums.Pace;
import ru.kholodov.locationcontextservice.services.LocationContextService;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты контроллера {@link LocationContextController}.
 *
 * <p>Проверяют корректность обработки запросов на построение маршрута,
 * валидацию входных данных и маппинг ответов.
 */
@WebMvcTest(LocationContextController.class)
class LocationContextControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private LocationContextService service;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("POST /api/v1/route/build — успешное построение маршрута")
  void buildRoute_ShouldReturnPlanData() throws Exception {
    // given
    LocationContextRequest request = new LocationContextRequest();
    request.setLocation("Арбат, Москва");
    request.setStartTime(LocalTime.of(10, 0));
    request.setEndTime(LocalTime.of(16, 0));
    request.setPace(Pace.MEDIUM);

    PlanBuildResponse.PlanItem item1 = new PlanBuildResponse.PlanItem(
            "osm_museum_123", "Пушкинский музей", "10:15", "12:45", "museum", 15);
    PlanBuildResponse.PlanItem item2 = new PlanBuildResponse.PlanItem(
            "osm_cafe_456", "Кафе «Уголёк»", "13:00", "14:00", "cafe", 10);

    PlanBuildResponse.PlanData mockPlan = new PlanBuildResponse.PlanData(
            List.of(item1, item2), 2, 3.5, List.of(), 24);

    when(service.buildRoute(any(LocationContextRequest.class))).thenReturn(mockPlan);

    // when & then
    mockMvc.perform(post("/api/v1/route/build")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalPlaces").value(2))
            .andExpect(jsonPath("$.totalHours").value(3.5))
            .andExpect(jsonPath("$.items[0].placeName").value("Пушкинский музей"))
            .andExpect(jsonPath("$.items[0].arrivalTime").value("10:15"))
            .andExpect(jsonPath("$.items[1].placeName").value("Кафе «Уголёк»"))
            .andExpect(jsonPath("$.evaluatedOrderings").value(24));
  }

  @Test
  @DisplayName("POST /api/v1/route/build — пустой план, если мест не найдено")
  void buildRoute_WhenNoPlacesFound_ShouldReturnEmptyPlan() throws Exception {
    // given
    LocationContextRequest request = new LocationContextRequest();
    request.setLocation("Пустырь, Москва");
    request.setStartTime(LocalTime.of(10, 0));
    request.setEndTime(LocalTime.of(12, 0));
    request.setPace(Pace.SLOW);

    PlanBuildResponse.PlanData emptyPlan = new PlanBuildResponse.PlanData(
            List.of(), 0, 0.0, List.of("По заданным параметрам не найдено подходящих мест"), 0);

    when(service.buildRoute(any(LocationContextRequest.class))).thenReturn(emptyPlan);

    // when & then
    mockMvc.perform(post("/api/v1/route/build")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalPlaces").value(0))
            .andExpect(jsonPath("$.warnings[0]").value("По заданным параметрам не найдено подходящих мест"));
  }

  @Test
  @DisplayName("POST /api/v1/route/build — ошибка валидации: пустой адрес")
  void buildRoute_WhenEmptyLocation_ShouldReturn400() throws Exception {
    // given
    LocationContextRequest request = new LocationContextRequest();
    request.setLocation(""); // невалидно
    request.setStartTime(LocalTime.of(10, 0));
    request.setEndTime(LocalTime.of(16, 0));
    request.setPace(Pace.MEDIUM);

    // when & then
    mockMvc.perform(post("/api/v1/route/build")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors[0].message").exists());
  }

  @Test
  @DisplayName("POST /api/v1/route/build — ошибка валидации: endTime раньше startTime")
  void buildRoute_WhenEndTimeBeforeStartTime_ShouldReturn400() throws Exception {
    // given
    LocationContextRequest request = new LocationContextRequest();
    request.setLocation("Арбат, Москва");
    request.setStartTime(LocalTime.of(18, 0));
    request.setEndTime(LocalTime.of(10, 0)); // раньше startTime
    request.setPace(Pace.MEDIUM);

    // when & then
    mockMvc.perform(post("/api/v1/route/build")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            // Проверяем, что ошибка содержит нужное сообщение (с учётом префикса поля)
            .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors[0].message").value(
                    "endTimeAfterStartTime: Время окончания должно быть позже времени начала"));
  }

  @Test
  @DisplayName("POST /api/v1/route/build — адрес не найден, возврат 404")
  void buildRoute_WhenAddressNotFound_ShouldReturn404() throws Exception {
    // given
    LocationContextRequest request = new LocationContextRequest();
    request.setLocation("Несуществующий адрес 12345");
    request.setStartTime(LocalTime.of(10, 0));
    request.setEndTime(LocalTime.of(16, 0));
    request.setPace(Pace.MEDIUM);

    when(service.buildRoute(any(LocationContextRequest.class)))
            .thenThrow(new ru.kholodov.locationcontextservice.exception.AddressNotFoundException(
                    "Не удалось найти координаты для адреса: Несуществующий адрес 12345"));

    // when & then
    mockMvc.perform(post("/api/v1/route/build")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errors[0].code").value("ADDRESS_NOT_FOUND"))
            .andExpect(jsonPath("$.errors[0].message").value(
                    "Не удалось найти координаты для адреса: Несуществующий адрес 12345"));
  }
}