package com.travelplanner.planner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PlannerControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void healthReturnsOkWithStatus() throws Exception {
    mockMvc
        .perform(get("/api/v1/plan/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"));
  }

  @Test
  void buildReturnsOkWithNonEmptyItems() throws Exception {
    String body =
        """
				{
				  "startTime": "10:00",
				  "endTime": "18:00",
				  "places": [
				    {
				      "placeId": "place_001",
				      "name": "Третьяковская галерея",
				      "category": "gallery",
				      "latitude": 55.7413,
				      "longitude": 37.6204,
				      "estimatedHours": 2.5,
				      "description": "",
				      "rating": 4.8
				    }
				  ]
				}
				""";
    mockMvc
        .perform(post("/api/v1/plan/build").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items").isArray())
        .andExpect(jsonPath("$.data.items.length()").value(1))
        .andExpect(jsonPath("$.data.items[0].travelTimeMinutes").value(0))
        .andExpect(jsonPath("$.data.totalPlaces").value(1))
        .andExpect(jsonPath("$.data.warnings").isArray())
        .andExpect(jsonPath("$.data.evaluatedOrderings").value(1));
  }

  @Test
  void buildShouldReturn400WhenPlacesEmpty() throws Exception {
    String body =
        """
				{
				  "startTime": "10:00",
				  "endTime": "18:00",
				  "places": []
				}
				""";
    mockMvc
        .perform(post("/api/v1/plan/build").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.errors[0].message").exists());
  }

  @Test
  void buildShouldWarnWhenPlaceDoesNotFit() throws Exception {
    String body =
        """
				{
				  "startTime": "10:00",
				  "endTime": "11:00",
				  "places": [{
				    "placeId": "p1",
				    "name": "Третьяковка",
				    "category": "gallery",
				    "latitude": 55.74,
				    "longitude": 37.62,
				    "estimatedHours": 5.0,
				    "description": "",
				    "rating": 4.8
				  }]
				}
				""";
    mockMvc
        .perform(post("/api/v1/plan/build").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items").isEmpty())
        .andExpect(jsonPath("$.data.warnings").isNotEmpty())
        .andExpect(jsonPath("$.data.evaluatedOrderings").value(1));
  }

  @Test
  void buildShouldReturn400WhenTimeFormatInvalid() throws Exception {
    String body =
        """
				{
				  "startTime": "утро",
				  "endTime": "вечер",
				  "places": [{
				    "placeId": "p1",
				    "name": "Место",
				    "category": "park",
				    "latitude": 55.74,
				    "longitude": 37.62,
				    "estimatedHours": 1.0,
				    "description": "",
				    "rating": 4.0
				  }]
				}
				""";
    mockMvc
        .perform(post("/api/v1/plan/build").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.errors[0].message").exists());
  }

  @Test
  void buildShouldScheduleSeveralPlacesWithDifferentDurations() throws Exception {
    String body =
        """
				{
				  "startTime": "09:00",
				  "endTime": "14:00",
				  "places": [
				    {
				      "placeId": "gallery",
				      "name": "Галерея",
				      "category": "gallery",
				      "latitude": 55.7413,
				      "longitude": 37.6204,
				      "estimatedHours": 1.5,
				      "description": "",
				      "rating": 4.8
				    },
				    {
				      "placeId": "park",
				      "name": "Парк",
				      "category": "park",
				      "latitude": 55.7517,
				      "longitude": 37.6288,
				      "estimatedHours": 0.75,
				      "description": "",
				      "rating": 4.6
				    },
				    {
				      "placeId": "museum",
				      "name": "Музей",
				      "category": "museum",
				      "latitude": 55.7600,
				      "longitude": 37.6400,
				      "estimatedHours": 2.0,
				      "description": "",
				      "rating": 4.5
				    }
				  ]
				}
				""";
    mockMvc
        .perform(post("/api/v1/plan/build").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(3))
        .andExpect(jsonPath("$.data.items[0].arrivalTime").value("09:00"))
        .andExpect(jsonPath("$.data.items[0].departureTime").value("10:30"))
        .andExpect(jsonPath("$.data.items[0].travelTimeMinutes").value(0))
        .andExpect(jsonPath("$.data.items[1].arrivalTime").value("10:46"))
        .andExpect(jsonPath("$.data.items[1].departureTime").value("11:31"))
        .andExpect(jsonPath("$.data.items[1].travelTimeMinutes").value(16))
        .andExpect(jsonPath("$.data.items[2].arrivalTime").value("11:45"))
        .andExpect(jsonPath("$.data.items[2].departureTime").value("13:45"))
        .andExpect(jsonPath("$.data.items[2].travelTimeMinutes").value(14))
        .andExpect(jsonPath("$.data.totalHours").value(4.25))
        .andExpect(jsonPath("$.data.warnings").isEmpty())
        .andExpect(jsonPath("$.data.evaluatedOrderings").value(6));
  }

  @Test
  void buildShouldIncludeWalkingTimeFromStartCoordinates() throws Exception {
    String body =
        """
				{
				  "startTime": "10:00",
				  "endTime": "12:00",
				  "startLatitude": 55.7500,
				  "startLongitude": 37.6000,
				  "places": [{
				    "placeId": "nearby",
				    "name": "Ближайшее место",
				    "category": "park",
				    "latitude": 55.7500,
				    "longitude": 37.6000,
				    "estimatedHours": 1.0,
				    "description": "",
				    "rating": 4.0,
				    "openingHoursText": "09:00-21:00"
				  }]
				}
				""";
    mockMvc
        .perform(post("/api/v1/plan/build").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(1))
        .andExpect(jsonPath("$.data.items[0].arrivalTime").value("10:00"))
        .andExpect(jsonPath("$.data.items[0].departureTime").value("11:00"))
        .andExpect(jsonPath("$.data.items[0].travelTimeMinutes").value(0));
  }
}
