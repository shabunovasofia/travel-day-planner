package ru.kholodov.locationcontextservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "geocoding.api-key=test-key",
        "geocoding.url=https://us1.locationiq.com/v1/search"
})
class LocationContextServiceApplicationTests {
  @Test
  void contextLoads() {
  }
}
