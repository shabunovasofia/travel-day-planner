package com.travelplanner.planner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlannerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthReturnsOkWithStatus() throws Exception {
		mockMvc.perform(get("/api/v1/plan/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ok"));
	}

	@Test
	void buildReturnsOkWithNonEmptyItems() throws Exception {
		String body = """
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
		mockMvc.perform(post("/api/v1/plan/build")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isArray())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.totalPlaces").value(1))
				.andExpect(jsonPath("$.warnings").isArray())
				.andExpect(jsonPath("$.evaluatedOrderings").value(1));
	}

	@Test
	void buildShouldReturn400WhenPlacesEmpty() throws Exception {
		String body = """
				{
				  "startTime": "10:00",
				  "endTime": "18:00",
				  "places": []
				}
				""";
		mockMvc.perform(post("/api/v1/plan/build")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void buildShouldWarnWhenPlaceDoesNotFit() throws Exception {
		String body = """
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
		mockMvc.perform(post("/api/v1/plan/build")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isEmpty())
				.andExpect(jsonPath("$.warnings").isNotEmpty())
				.andExpect(jsonPath("$.evaluatedOrderings").value(1));
	}

	@Test
	void buildShouldReturn400WhenTimeFormatInvalid() throws Exception {
		String body = """
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
		mockMvc.perform(post("/api/v1/plan/build")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void buildShouldScheduleSeveralPlacesWithDifferentDurations() throws Exception {
		String body = """
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
		mockMvc.perform(post("/api/v1/plan/build")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(3))
				.andExpect(jsonPath("$.items[0].arrivalTime").value("09:00"))
				.andExpect(jsonPath("$.items[0].departureTime").value("10:30"))
				.andExpect(jsonPath("$.items[1].arrivalTime").value("10:30"))
				.andExpect(jsonPath("$.items[1].departureTime").value("11:15"))
				.andExpect(jsonPath("$.items[2].arrivalTime").value("11:15"))
				.andExpect(jsonPath("$.items[2].departureTime").value("13:15"))
				.andExpect(jsonPath("$.totalHours").value(4.25))
				.andExpect(jsonPath("$.warnings").isEmpty())
				.andExpect(jsonPath("$.evaluatedOrderings").value(6));
	}
}
