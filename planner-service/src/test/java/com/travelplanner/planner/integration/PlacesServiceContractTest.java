package com.travelplanner.planner.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.planner.dto.PlaceDto;
import org.junit.jupiter.api.Test;

class PlacesServiceContractTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void placeDtoDeserializesFromPlacesServiceJson() throws Exception {
    String body =
        """
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
        """;

    PlaceDto place = objectMapper.readValue(body, PlaceDto.class);

    assertThat(place.getPlaceId()).isEqualTo("place_001");
    assertThat(place.getName()).isEqualTo("Третьяковская галерея");
    assertThat(place.getCategory()).isEqualTo("gallery");
    assertThat(place.getLatitude()).isEqualTo(55.7413);
    assertThat(place.getLongitude()).isEqualTo(37.6204);
    assertThat(place.getEstimatedHours()).isEqualTo(2.5);
    assertThat(place.getDescription()).isEqualTo("Художественный музей");
    assertThat(place.getRating()).isEqualTo(4.8);
  }

  @Test
  void placeDtoIgnoresUnknownFields() throws Exception {
    String body =
        """
        {
          "placeId": "place_002",
          "name": "Парк Горького",
          "category": "park",
          "latitude": 55.7300,
          "longitude": 37.6013,
          "estimatedHours": 1.5,
          "unknownField": "ignored"
        }
        """;

    PlaceDto place = objectMapper.readValue(body, PlaceDto.class);

    assertThat(place.getPlaceId()).isEqualTo("place_002");
    assertThat(place.getName()).isEqualTo("Парк Горького");
  }
}
