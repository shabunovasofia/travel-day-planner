package ru.kholodov.locationcontextservice.client;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.kholodov.locationcontextservice.config.IntegrationProperties;
import ru.kholodov.locationcontextservice.dto.places.PlacesSearchRequest;
import ru.kholodov.locationcontextservice.dto.places.PlacesSearchResponse;
import ru.kholodov.locationcontextservice.dto.planner.PlanBuildRequest;
import ru.kholodov.locationcontextservice.dto.planner.PlanBuildResponse;

/**
 * Клиент для HTTP-вызовов к {@code places-service} и {@code planner-service}.
 *
 * <p>Использует {@link RestClient} с настроенными таймаутами из {@link IntegrationProperties}.
 * Транзиентные ошибки upstream-сервисов обрабатываются Resilience4j (retry + circuit breaker).
 * Для {@link #searchPlaces} при исчерпании попыток / открытом CB возвращается пустой список;
 * для {@link #buildPlan} ошибка пробрасывается выше (план без planner-сервиса не построить).
 */
@Slf4j
@Component
public class UpstreamServicesClient {

    private static final String PLACES_RESILIENCE = "places";
    private static final String PLANNER_RESILIENCE = "planner";

    private final RestClient placesClient;
    private final RestClient plannerClient;

    public UpstreamServicesClient(
            RestClient.Builder restClientBuilder,
            IntegrationProperties props) {

        this.placesClient = restClientBuilder
                .baseUrl(trimSlash(props.getPlacesBaseUrl()))
                .build();

        this.plannerClient = restClientBuilder
                .baseUrl(trimSlash(props.getPlannerBaseUrl()))
                .build();
    }


    /**
     * Вызывает places-service для поиска мест по координатам.
     *
     * @param request параметры поиска
     * @return список найденных мест
     */
    @Retry(name = PLACES_RESILIENCE)
    @CircuitBreaker(name = PLACES_RESILIENCE, fallbackMethod = "searchPlacesFallback")
    public List<PlacesSearchResponse.PlaceDto> searchPlaces(PlacesSearchRequest request) {
        log.debug("Вызов places-service: {}", request);
        var response = placesClient.post()
                .uri("/api/v1/places/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PlacesSearchResponse.class);

        if (response == null || response.getData() == null || response.getData().getPlaces() == null) {
            return List.of();
        }
        return response.getData().getPlaces();
    }

    @SuppressWarnings("unused")
    private List<PlacesSearchResponse.PlaceDto> searchPlacesFallback(
            PlacesSearchRequest request, Throwable t) {
        log.error("places-service fallback (CB open / retries exhausted): {}", t.toString());
        return List.of();
    }

    /**
     * Вызывает planner-service для построения маршрута.
     *
     * @param request параметры планирования
     * @return готовый план с расписанием
     */
    @Retry(name = PLANNER_RESILIENCE)
    @CircuitBreaker(name = PLANNER_RESILIENCE)
    public PlanBuildResponse.PlanData buildPlan(PlanBuildRequest request) {
        log.debug("Вызов planner-service: {}", request);
        var response = plannerClient.post()
                .uri("/api/v1/plan/build")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PlanBuildResponse.class);

        if (response == null || response.getData() == null) {
            throw new IllegalStateException("Пустой ответ от planner-service");
        }
        return response.getData();
    }

    private static String trimSlash(String url) {
        if (url == null || url.isBlank()) return "http://localhost:8082";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
