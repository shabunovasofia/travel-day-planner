package com.travelplanner.places;

import com.travelplanner.places.controller.PlacesController;
import com.travelplanner.places.dto.PlacesSearchResponse;
import com.travelplanner.places.service.PlacesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlacesController.class)
class PlacesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlacesService placesService;

    @Test
    void searchShouldReturnOk() throws Exception {
        when(placesService.search(any())).thenReturn(new PlacesSearchResponse(List.of(), 0));

        mockMvc.perform(post("/api/v1/places/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\":55.75,\"longitude\":37.61,\"radiusMeters\":3000,\"availableHours\":6.0}"))
                .andExpect(status().isOk());
    }
}
