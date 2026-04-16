package ru.kholodov.locationcontextservice.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kholodov.locationcontextservice.LocationContextRequest;
import ru.kholodov.locationcontextservice.LocationContextResponse;
import ru.kholodov.locationcontextservice.service.LocationContextService;

@RestController
@RequestMapping("/api/v1/context")
public class LocationContextController {

    private static final Logger logger = LoggerFactory.getLogger(LocationContextController.class);
    private final LocationContextService locationContextService;


    public LocationContextController(LocationContextService locationContextService) {
        this.locationContextService = locationContextService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<LocationContextResponse> getLocation(
            @Valid @RequestBody LocationContextRequest request) {
        logger.info("Вызван метод getLocation");
        LocationContextResponse response = locationContextService.getLocation(request);
        return ResponseEntity.ok(response);
    }


}
