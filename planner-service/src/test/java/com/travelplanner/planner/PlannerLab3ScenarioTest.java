package com.travelplanner.planner;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
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
 * Тестовые сценарии ЛР3 (участник 2): поведение при опциональной интеграции без поднятых внешних
 * сервисов.
 *
 * <p>Сценарий A: обычный план — только поля времени и {@code places}.
 *
 * <p>Сценарий B: указаны {@code walkLocation} и {@code discoverCategories}, но {@code
 * planner.integration.enabled=false} — HTTP к location-context и places не выполняется, в ответе
 * есть предупреждение.
 */
@SpringBootTest(properties = "planner.integration.enabled=false")
@AutoConfigureMockMvc
class PlannerLab3ScenarioTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName(
      "Сценарий B: walkLocation при выключенной интеграции — предупреждение, план строится по places")
  void scenarioB_discoverySkippedWhenIntegrationDisabled() throws Exception {
    String body =
        """
				{
				  "startTime": "10:00",
				  "endTime": "14:00",
				  "walkLocation": "Арбат, Москва",
				  "walkPace": "MEDIUM",
				  "discoverCategories": ["museum", "park"],
				  "places": [{
				    "placeId": "local_1",
				    "name": "Локальное место",
				    "category": "park",
				    "latitude": 55.75,
				    "longitude": 37.61,
				    "estimatedHours": 1.0,
				    "description": "",
				    "rating": 4.2
				  }]
				}
				""";
    mockMvc
        .perform(post("/api/v1/plan/build").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.warnings[*]", hasItem(containsString("Интеграция"))))
        .andExpect(jsonPath("$.data.items.length()").value(1));
  }
}
