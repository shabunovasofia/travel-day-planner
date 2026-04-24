package com.travelplanner.places;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.travelplanner.places.controller.PlacesController;
import com.travelplanner.places.dto.PlaceDto;
import com.travelplanner.places.dto.PlacesSearchResponse;
import com.travelplanner.places.service.PlacesService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlacesController.class)
class PlacesControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PlacesService placesService;

  @Test
  void searchShouldReturnOk() throws Exception {
    when(placesService.search(any())).thenReturn(new PlacesSearchResponse(List.of(), 0));

    mockMvc
        .perform(
            post("/api/v1/places/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"latitude\":55.75,\"longitude\":37.61,"
                        + "\"radiusMeters\":3000,\"availableHours\":6.0}"))
        .andExpect(status().isOk());
  }

  @Test
  void searchShouldReturnPlaces() throws Exception {
    PlaceDto place =
        new PlaceDto(
            "osm_museum_0", "Третьяковская галерея", "museum", 55.7415, 37.6208, 2.5, 4.5, "", "");

    when(placesService.search(any())).thenReturn(new PlacesSearchResponse(List.of(place), 1));

    mockMvc
        .perform(
            post("/api/v1/places/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "latitude": 55.75,
                                  "longitude": 37.61,
                                  "radiusMeters": 3000,
                                  "availableHours": 6.0,
                                  "categories": ["museum"]
                                }
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalFound").value(1))
        .andExpect(jsonPath("$.places").isArray())
        .andExpect(jsonPath("$.places[0].name").value("Третьяковская галерея"))
        .andExpect(jsonPath("$.places[0].category").value("museum"))
        .andExpect(jsonPath("$.places[0].estimatedHours").value(2.5));
  }

  @Test
  void searchShouldReturnPlaceWithRatingAndDescription() throws Exception {
    PlaceDto place =
        new PlaceDto(
            "otm_museum_0",
            "Третьяковская галерея",
            "museum",
            55.7415,
            37.6208,
            2.5,
            4.5, // rating
            "Лаврушинский переулок, 10, Москва", // address
            "Знаменитый музей русского искусства"); // description

    when(placesService.search(any())).thenReturn(new PlacesSearchResponse(List.of(place), 1));

    mockMvc
        .perform(
            post("/api/v1/places/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "latitude": 55.75,
                                  "longitude": 37.61,
                                  "radiusMeters": 3000,
                                  "availableHours": 6.0,
                                  "categories": ["museum"]
                                }
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.places[0].rating").value(4.5))
        .andExpect(jsonPath("$.places[0].description").isNotEmpty())
        .andExpect(jsonPath("$.places[0].address").isNotEmpty());
  }

  @Test
  void searchShouldReturnMultiplePlacesFromDifferentCategories() throws Exception {
    PlaceDto museum =
        new PlaceDto("otm_museum_0", "Эрмитаж", "museum", 59.94, 30.31, 2.5, 5.0, "", "");
    PlaceDto park =
        new PlaceDto("otm_park_0", "Летний сад", "park", 59.94, 30.33, 1.5, 4.0, "", "");

    when(placesService.search(any()))
        .thenReturn(new PlacesSearchResponse(List.of(museum, park), 2));

    mockMvc
        .perform(
            post("/api/v1/places/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "latitude": 59.94,
                                  "longitude": 30.31,
                                  "radiusMeters": 3000,
                                  "availableHours": 6.0,
                                  "categories": ["museum", "park"]
                                }
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalFound").value(2))
        .andExpect(jsonPath("$.places[0].category").value("museum"))
        .andExpect(jsonPath("$.places[1].category").value("park"));
  }

  @Test
  void searchWithoutCategoriesShouldReturnOk() throws Exception {
    when(placesService.search(any())).thenReturn(new PlacesSearchResponse(List.of(), 0));

    mockMvc
        .perform(
            post("/api/v1/places/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "latitude": 55.75,
                                  "longitude": 37.61,
                                  "radiusMeters": 3000,
                                  "availableHours": 6.0
                                }
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.places").isArray());
  }
}
