package com.travelplanner.planner;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Тестовые сценарии ЛР3 (участник 2): построение маршрута с несколькими местами.
 *
 * <p>Сценарий A: обычный план — одно место, план строится корректно.
 *
 * <p>Сценарий B: несколько мест разных категорий — расписание включает все места
 * и кафе не идут подряд.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PlannerLab3ScenarioTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Сценарий A: одно место — план строится, totalPlaces=1")
  void scenarioA_singlePlaceBuildsSchedule() throws Exception {
    String body =
        """
        {
          "startTime": "10:00",
          "endTime": "14:00",
          "startLatitude": 55.75,
          "startLongitude": 37.61,
          "places": [{
            "placeId": "local_1",
            "name": "Локальное место",
            "category": "park",
            "latitude": 55.75,
            "longitude": 37.61,
            "estimatedHours": 1.0,
            "rating": 4.2
          }]
        }
        """;
    mockMvc
        .perform(post("/api/v1/plan/build").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalPlaces").value(1))
        .andExpect(jsonPath("$.data.items", hasSize(1)))
        .andExpect(jsonPath("$.data.items[0].placeId").value("local_1"));
  }

  @Test
  @DisplayName("Сценарий B: музей + парк + кафе — все три места входят в расписание")
  void scenarioB_mixedCategoriesAllIncluded() throws Exception {
    String body =
        """
        {
          "startTime": "10:00",
          "endTime": "18:00",
          "startLatitude": 55.75,
          "startLongitude": 37.61,
          "places": [
            {
              "placeId": "museum_1",
              "name": "Третьяковская галерея",
              "category": "museum",
              "latitude": 55.7415,
              "longitude": 37.6208,
              "estimatedHours": 2.5,
              "rating": 4.8
            },
            {
              "placeId": "park_1",
              "name": "Парк Горького",
              "category": "park",
              "latitude": 55.7300,
              "longitude": 37.6013,
              "estimatedHours": 1.5,
              "rating": 4.7
            },
            {
              "placeId": "cafe_1",
              "name": "Кафе Пушкин",
              "category": "cafe",
              "latitude": 55.7647,
              "longitude": 37.6055,
              "estimatedHours": 1.0,
              "rating": 4.9
            }
          ]
        }
        """;
    mockMvc
        .perform(post("/api/v1/plan/build").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalPlaces").value(3))
        .andExpect(jsonPath("$.data.items", hasSize(3)));
  }
}
