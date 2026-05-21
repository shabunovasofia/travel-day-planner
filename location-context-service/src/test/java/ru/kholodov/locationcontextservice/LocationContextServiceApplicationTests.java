package ru.kholodov.locationcontextservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
    properties = {
      "geocoding.api-key=test-key",
      "geocoding.url=https://us1.locationiq.com/v1/search",
      "isochrone.api-key=test-key",
      "isochrone.url=https://api.openrouteservice.org/v2/isochrones/foot-walking"
    })
class LocationContextServiceApplicationTests {
  @Test
  void contextLoads() {}
}
