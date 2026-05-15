package com.travelplanner.planner.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.planner.integration.dto.PlacesSearchApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlacesServiceContractTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void placesServiceResponseDeserializesIntoPlannerPlaceDto() throws Exception {
		String body = """
				{
				  "places": [
				    {
				      "placeId": "place_001",
				      "name": "Третьяковская галерея",
				      "category": "gallery",
				      "latitude": 55.7413,
				      "longitude": 37.6204,
				      "estimatedHours": 2.5,
				      "description": "Художественный музей",
				      "rating": 4.8
				    }
				  ],
				  "totalFound": 1
				}
				""";

		PlacesSearchApiResponse response = objectMapper.readValue(body, PlacesSearchApiResponse.class);

		assertThat(response.getTotalFound()).isEqualTo(1);
		assertThat(response.getPlaces()).hasSize(1);
		assertThat(response.getPlaces().get(0).getPlaceId()).isEqualTo("place_001");
		assertThat(response.getPlaces().get(0).getName()).isEqualTo("Третьяковская галерея");
		assertThat(response.getPlaces().get(0).getCategory()).isEqualTo("gallery");
		assertThat(response.getPlaces().get(0).getLatitude()).isEqualTo(55.7413);
		assertThat(response.getPlaces().get(0).getLongitude()).isEqualTo(37.6204);
		assertThat(response.getPlaces().get(0).getEstimatedHours()).isEqualTo(2.5);
		assertThat(response.getPlaces().get(0).getDescription()).isEqualTo("Художественный музей");
		assertThat(response.getPlaces().get(0).getRating()).isEqualTo(4.8);
	}
}
