package com.travelplanner.places.service;

import com.travelplanner.places.dto.PlaceDto;
import com.travelplanner.places.dto.PlacesSearchRequest;
import com.travelplanner.places.dto.PlacesSearchResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlacesService {

    public PlacesSearchResponse search(PlacesSearchRequest request){
        List < PlaceDto> places = List.of(
                new PlaceDto("place_001", "Третьяковская галерея", "gallery", 55.7415, 37.6208, 2.5, "Крупнейший музей русского искусства", 4.8),
                new PlaceDto("place_002", "Парк Горького", "park", 55.7454, 37.6689, 1.5, "Центральный парк культуры и отдыха", 4.8)
                );


        return new PlacesSearchResponse(places, places.size());
    }


}
