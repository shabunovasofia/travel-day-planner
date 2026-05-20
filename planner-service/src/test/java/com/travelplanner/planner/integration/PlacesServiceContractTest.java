package com.travelplanner.planner.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.planner.integration.dto.LocationContextAnalyzeResponse;
import com.travelplanner.planner.integration.dto.PlacesSearchApiResponse;
import org.junit.jupiter.api.Test;

class PlacesServiceContractTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void placesServiceResponseDeserializesIntoPlannerPlaceDto() throws Exception {
    String body =
        """
				{
				  "data": {
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
				}
				""";

    PlacesSearchApiResponse response = objectMapper.readValue(body, PlacesSearchApiResponse.class);

    assertThat(response.getData().getTotalFound()).isEqualTo(1);
    assertThat(response.getData().getPlaces()).hasSize(1);
    assertThat(response.getData().getPlaces().get(0).getPlaceId()).isEqualTo("place_001");
    assertThat(response.getData().getPlaces().get(0).getName()).isEqualTo("Третьяковская галерея");
    assertThat(response.getData().getPlaces().get(0).getCategory()).isEqualTo("gallery");
    assertThat(response.getData().getPlaces().get(0).getLatitude()).isEqualTo(55.7413);
    assertThat(response.getData().getPlaces().get(0).getLongitude()).isEqualTo(37.6204);
    assertThat(response.getData().getPlaces().get(0).getEstimatedHours()).isEqualTo(2.5);
    assertThat(response.getData().getPlaces().get(0).getDescription())
        .isEqualTo("Художественный музей");
    assertThat(response.getData().getPlaces().get(0).getRating()).isEqualTo(4.8);
  }

  @Test
  void locationContextResponseDeserializesFromDataWrapperAndKeepsProxyGetters() throws Exception {
    String body =
        """
				{
				  "data": {
				    "latitude": 55.7512,
				    "longitude": 37.6184,
				    "radiusMeters": 2500,
				    "availableHours": 4.5
				  }
				}
				""";

    LocationContextAnalyzeResponse response =
        objectMapper.readValue(body, LocationContextAnalyzeResponse.class);

    assertThat(response.getData()).isNotNull();
    assertThat(response.getLatitude()).isEqualTo(55.7512);
    assertThat(response.getLongitude()).isEqualTo(37.6184);
    assertThat(response.getRadiusMeters()).isEqualTo(2500);
    assertThat(response.getAvailableHours()).isEqualTo(4.5);
  }
}
