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
				.andExpect(jsonPath("$.warnings").isArray());
	}
}
