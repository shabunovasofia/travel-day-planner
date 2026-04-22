package ru.kholodov.locationcontextservice.controllerTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.kholodov.locationcontextservice.controller.LocationContextController;
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.LocationContextResponse;
import ru.kholodov.locationcontextservice.enums.Pace;
import ru.kholodov.locationcontextservice.services.LocationContextService;

@WebMvcTest(LocationContextController.class)
class LocationContextControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private LocationContextService service;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void getLocation_ShouldReturnResponse() throws Exception {
    LocationContextRequest request = new LocationContextRequest();
    request.setLocation("Арбат");
    request.setStartTime(LocalTime.of(10, 0));
    request.setEndTime(LocalTime.of(16, 0));
    request.setPace(Pace.MEDIUM);

    LocationContextResponse mockResponse =
        LocationContextResponse.create(
            "Арбат", 55.7520, 37.5921, LocalTime.of(10, 0), LocalTime.of(16, 0), Pace.MEDIUM);

    when(service.getLocation(any(LocationContextRequest.class))).thenReturn(mockResponse);

    mockMvc
        .perform(
            post("/api/v1/context/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resolvedLocation").value("Арбат"))
        .andExpect(jsonPath("$.radiusMeters").value(3000))
        .andExpect(jsonPath("$.startTime").value("10:00"));
  }

  @Test
  void getLocation_WhenInvalidRequest_ShouldReturn400() throws Exception {
    LocationContextRequest request = new LocationContextRequest();
    request.setLocation(""); // невалидно
    request.setStartTime(LocalTime.of(10, 0));
    request.setEndTime(LocalTime.of(16, 0));
    request.setPace(Pace.MEDIUM);

    mockMvc
        .perform(
            post("/api/v1/context/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
