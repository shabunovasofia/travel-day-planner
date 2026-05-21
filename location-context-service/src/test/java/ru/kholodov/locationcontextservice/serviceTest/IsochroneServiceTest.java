package ru.kholodov.locationcontextservice.serviceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import ru.kholodov.locationcontextservice.config.IsochroneProperties;
import ru.kholodov.locationcontextservice.dto.Coordinates;
import ru.kholodov.locationcontextservice.enums.Pace;
import ru.kholodov.locationcontextservice.services.IsochroneService;

class IsochroneServiceTest {

  private static final String POLYGON_RESPONSE =
      """
            {
              "features": [{
                "geometry": {
                  "type": "Polygon",
                  "coordinates": [[[37.61,55.76],[37.62,55.76],[37.62,55.75],[37.61,55.75],[37.61,55.76]]]
                }
              }]
            }
            """;

  private MockRestServiceServer mockServer;
  private IsochroneService isochroneService;

  @BeforeEach
  void setUp() {
    IsochroneProperties props = new IsochroneProperties();
    props.setUrl("https://api.openrouteservice.org/v2/isochrones/foot-walking");
    props.setApiKey("test-key");

    RestClient.Builder builder = RestClient.builder();
    mockServer = MockRestServiceServer.bindTo(builder).build();
    isochroneService = new IsochroneService(builder, props);
  }

  @Test
  void calculateRadius_ShouldReturnDistance_WhenPolygonReturned() {
    mockServer
        .expect(requestTo(containsString("openrouteservice.org")))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(POLYGON_RESPONSE, MediaType.APPLICATION_JSON));

    Optional<Double> result =
        isochroneService.calculateRadius(new Coordinates(55.755, 37.615), 2.0, Pace.MEDIUM);

    assertThat(result).isPresent();
    assertThat(result.get()).isGreaterThan(0);
    mockServer.verify();
  }

  @Test
  void calculateRadius_ShouldPropagate5xx_ForRetryHandling() {
    // 5xx больше не глотается внутри сервиса — пробрасывается наружу,
    // чтобы Resilience4j (@Retry / @CircuitBreaker) мог среагировать.
    // Fallback вернёт Optional.empty() уже на уровне Spring-прокси.
    mockServer
        .expect(requestTo(containsString("openrouteservice.org")))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withServerError());

    assertThatThrownBy(
            () -> isochroneService.calculateRadius(new Coordinates(55.755, 37.615), 2.0, Pace.SLOW))
        .isInstanceOf(HttpServerErrorException.class);
    mockServer.verify();
  }

  @Test
  void calculateRadius_ShouldReturnEmpty_WhenTimeTooSmall() {
    // availableHours=0.01, SLOW: rangeSeconds = 0.01 * 3600 * 3.5/5.0 = 25 < 60
    Optional<Double> result =
        isochroneService.calculateRadius(new Coordinates(55.755, 37.615), 0.01, Pace.SLOW);

    assertThat(result).isEmpty();
  }

  @Test
  void calculateRadius_ShouldReturnEmpty_WhenFeaturesArrayIsEmpty() {
    mockServer
        .expect(requestTo(containsString("openrouteservice.org")))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"features\": []}", MediaType.APPLICATION_JSON));

    Optional<Double> result =
        isochroneService.calculateRadius(new Coordinates(55.755, 37.615), 2.0, Pace.MEDIUM);

    assertThat(result).isEmpty();
    mockServer.verify();
  }
}
