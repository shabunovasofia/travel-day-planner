package ru.kholodov.locationcontextservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.kholodov.locationcontextservice.dto.Coordinates;
import ru.kholodov.locationcontextservice.dto.GeocodingResponse;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeocodingService {
    @Value("${geocoding.url}")
    private String geocodingUrl;

    @Value("${geocoding.api-key}")
    private String apiKey;
    private final Map<String, Optional<Coordinates>> cache = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(GeocodingService.class);

    private final RestTemplate restTemplate;

    public GeocodingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Преобразует текстовый адрес в координаты.
     * @param address адрес в свободной форме (например "Арбат, Москва")
     * @return Optional с координатами, либо пустой Optional, если адрес не найден или произошла ошибка
     */
    public Optional<Coordinates> geocode(String address) {
        return cache.computeIfAbsent(address, this::geocodeUncached);
    }

    private Optional<Coordinates> geocodeUncached(String address) {
        try {

            URI uri = UriComponentsBuilder.fromHttpUrl(geocodingUrl)
                    .queryParam("key", apiKey)
                    .queryParam("q", address)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();                          // <-- URI, не String

            logger.debug("Geocoding request: q={}", address);   // вместо request URI: {}
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, "TravelDayPlanner/1.0 kholodov_stepan@inbox.ru");

            headers.set(HttpHeaders.ACCEPT_LANGUAGE, "ru,en;q=0.8");
            headers.set(HttpHeaders.ACCEPT, "application/json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);


            ResponseEntity<GeocodingResponse[]> response = restTemplate.exchange(
                    uri,                               // <-- и сюда URI
                    HttpMethod.GET,
                    entity,
                    GeocodingResponse[].class
            );

            // 5. Обрабатываем ответ
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().length > 0) {
                GeocodingResponse first = response.getBody()[0];
                double lat = Double.parseDouble(first.getLat());
                double lon = Double.parseDouble(first.getLon());
                logger.info("Успешно получены координаты для '{}': lat={}, lon={}", address, lat, lon);
                return Optional.of(new Coordinates(lat, lon));
            } else {
                logger.warn(" пустой результат для '{}'", address);
            }
        }  catch (HttpClientErrorException.NotFound e) {

            logger.info("Адрес не найден: {}", address);
            return Optional.empty();
        } catch (HttpClientErrorException.TooManyRequests e) {
            logger.warn("Rate limit exceeded для адреса: {}", address);
            return Optional.empty();
        } catch (HttpClientErrorException.Unauthorized e) {
            logger.error("Неверный API key LocationIQ");
            return Optional.empty();
        } catch (HttpStatusCodeException e) {
            logger.error("LocationIQ вернул {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        }
        return Optional.empty();
    }


}