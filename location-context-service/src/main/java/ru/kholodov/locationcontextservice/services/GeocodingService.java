package ru.kholodov.locationcontextservice.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.kholodov.locationcontextservice.config.GeocodingProperties;
import ru.kholodov.locationcontextservice.dto.Coordinates;
import ru.kholodov.locationcontextservice.dto.GeocodingResponse;

import java.util.Optional;

/**
 * Сервис геокодирования адресов через внешний API (LocationIQ).
 *
 * <p>Инициализирует {@link RestClient} один раз с заданным baseUrl (включая API-ключ) и
 * заголовками по умолчанию. При каждом вызове {@link #geocode(String)} передаются только
 * изменяемые параметры запроса. Результаты кэшируются через Spring Cache (Caffeine) с TTL 1 час.
 */
@Slf4j
@Service
public class GeocodingService {

    private final RestClient restClient;

    public GeocodingService(RestClient.Builder builder, GeocodingProperties props) {
        String baseUrl =
                UriComponentsBuilder.fromUriString(props.getUrl())
                        .queryParam("key", props.getApiKey())
                        .toUriString();

        this.restClient =
                builder.baseUrl(baseUrl)
                        .defaultHeader(
                                HttpHeaders.USER_AGENT,
                                "TravelDayPlanner/1.0 kholodov_stepan@inbox.ru")
                        .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "ru,en;q=0.8")
                        .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                        .build();
    }

    /**
     * Преобразует текстовый адрес в координаты. Результат кэшируется по ключу адреса.
     *
     * @param address адрес в свободной форме (например "Арбат, Москва")
     * @return Optional с координатами, либо пустой Optional, если адрес не найден или произошла
     *     ошибка
     */
    @Cacheable(value = "geocoding", key = "#address")
    public Optional<Coordinates> geocode(String address) {
        try {
            log.debug("Geocoding request: q={}", address);

            GeocodingResponse[] responses =
                    restClient
                            .get()
                            .uri(
                                    b ->
                                            b.queryParam("q", address)
                                                    .queryParam("format", "json")
                                                    .queryParam("limit", 1)
                                                    .build())
                            .retrieve()
                            .body(GeocodingResponse[].class);

            if (responses != null && responses.length > 0) {
                GeocodingResponse first = responses[0];
                double lat = Double.parseDouble(first.getLat());
                double lon = Double.parseDouble(first.getLon());
                log.info(
                        "Успешно получены координаты для '{}': lat={}, lon={}", address, lat, lon);
                return Optional.of(new Coordinates(lat, lon));
            }

            log.warn("Пустой результат для '{}'", address);
        } catch (HttpClientErrorException.NotFound e) {
            log.info("Адрес не найден: {}", address);
            return Optional.empty();
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Rate limit exceeded для адреса: {}", address);
            return Optional.empty();
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Неверный API key LocationIQ");
            return Optional.empty();
        } catch (HttpStatusCodeException e) {
            log.error("LocationIQ вернул {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        }
        return Optional.empty();
    }
}
