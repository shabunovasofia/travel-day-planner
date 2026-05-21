package com.travelplanner.planner.controller;

import com.travelplanner.planner.dto.PlanRequest;
import com.travelplanner.planner.service.PlannerService;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plan")
public class PlannerController {

  private final PlannerService plannerService;

  public PlannerController(PlannerService plannerService) {
    this.plannerService = plannerService;
  }

  @PostMapping(
      value = "/build",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> build(@RequestBody PlanRequest request) {
    return ResponseEntity.ok(Map.of("data", plannerService.build(request)));
  }

  @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }
}
