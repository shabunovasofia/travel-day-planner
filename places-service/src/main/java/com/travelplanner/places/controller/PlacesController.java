package com.travelplanner.places.controller;

import com.travelplanner.places.dto.PlacesSearchRequest;
import com.travelplanner.places.dto.PlacesSearchResponse;
import com.travelplanner.places.service.PlacesService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/places")
public class PlacesController {

    private final PlacesService placesService;

    public PlacesController(PlacesService placesService) {
        this.placesService = placesService;
    }

    @PostMapping("/search")
    public PlacesSearchResponse searchResponse (@RequestBody PlacesSearchRequest request){
        return placesService.search(request);
    }
}


