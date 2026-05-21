package ru.kholodov.locationcontextservice.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.kholodov.locationcontextservice.config.IntegrationProperties;
import ru.kholodov.locationcontextservice.dto.places.PlacesSearchRequest;
import ru.kholodov.locationcontextservice.dto.places.PlacesSearchResponse;
import ru.kholodov.locationcontextservice.dto.planner.PlanBuildRequest;
import ru.kholodov.locationcontextservice.dto.planner.PlanBuildResponse;

import java.util.List;

/**
 * Клиент для HTTP-вызовов к {@code places-service} и {@code planner-service}.
 *
 * <p>Использует {@link RestClient} с настроенными таймаутами из {@link IntegrationProperties}.
 * Все методы выбрасывают {@link org.springframework.web.client.RestClientException} при ошибках.
 */
@Slf4j
@Component
public class UpstreamServicesClient {

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

    /**
     * Вызывает planner-service для построения маршрута.
     *
     * @param request параметры планирования
     * @return готовый план с расписанием
     */
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
