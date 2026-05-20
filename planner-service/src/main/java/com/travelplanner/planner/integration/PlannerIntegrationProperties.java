package com.travelplanner.planner.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "planner.integration")
public class PlannerIntegrationProperties {

  /** Вызовы location-context и places при построении плана. */
  private boolean enabled = true;

  private String locationContextBaseUrl = "http://localhost:8081";

  private String placesBaseUrl = "http://localhost:8082";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getLocationContextBaseUrl() {
    return locationContextBaseUrl;
  }

  public void setLocationContextBaseUrl(String locationContextBaseUrl) {
    this.locationContextBaseUrl = locationContextBaseUrl;
  }

  public String getPlacesBaseUrl() {
    return placesBaseUrl;
  }

  public void setPlacesBaseUrl(String placesBaseUrl) {
    this.placesBaseUrl = placesBaseUrl;
  }
}
