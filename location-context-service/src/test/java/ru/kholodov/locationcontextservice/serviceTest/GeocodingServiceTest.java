package ru.kholodov.locationcontextservice.serviceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.kholodov.locationcontextservice.config.GeocodingProperties;
import ru.kholodov.locationcontextservice.dto.Coordinates;
import ru.kholodov.locationcontextservice.services.GeocodingService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GeocodingServiceTest {

    private MockRestServiceServer mockServer;
    private GeocodingService geocodingService;

    @BeforeEach
    void setUp() {
        GeocodingProperties props = new GeocodingProperties();
        props.setUrl("https://us1.locationiq.com/v1/search");
        props.setApiKey("test-key");

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        geocodingService = new GeocodingService(builder, props);
    }

    @Test
    void geocode_ShouldReturnCoordinates_WhenAddressFound() {
        mockServer
                .expect(requestTo(containsString("locationiq.com")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withSuccess(
                                "[{\"lat\":\"55.757\",\"lon\":\"37.615\",\"display_name\":\"Тверская\"}]",
                                MediaType.APPLICATION_JSON));

        Optional<Coordinates> result = geocodingService.geocode("Тверская, Москва");

        assertThat(result).isPresent();
        assertThat(result.get().lat()).isEqualTo(55.757);
        assertThat(result.get().lon()).isEqualTo(37.615);
        mockServer.verify();
    }

    @Test
    void geocode_ShouldReturnEmpty_WhenAddressNotFound() {
        mockServer
                .expect(requestTo(containsString("locationiq.com")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());

        Optional<Coordinates> result = geocodingService.geocode("Несуществующее место");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void geocode_ShouldReturnEmpty_WhenRateLimitExceeded() {
        mockServer
                .expect(requestTo(containsString("locationiq.com")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withTooManyRequests());

        Optional<Coordinates> result = geocodingService.geocode("Любой адрес");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void geocode_ShouldReturnEmpty_WhenUnauthorized() {
        mockServer
                .expect(requestTo(containsString("locationiq.com")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withUnauthorizedRequest());

        Optional<Coordinates> result = geocodingService.geocode("Проверка ключа");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void geocode_ShouldReturnEmpty_WhenResponseIsEmpty() {
        mockServer
                .expect(requestTo(containsString("locationiq.com")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        Optional<Coordinates> result = geocodingService.geocode("Пустой ответ");

        assertThat(result).isEmpty();
        mockServer.verify();
    }
}
