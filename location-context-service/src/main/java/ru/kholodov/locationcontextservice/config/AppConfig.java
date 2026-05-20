package ru.kholodov.locationcontextservice.config;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Конфигурационный класс приложения.
 *
 * <p>Настраивает общий {@link org.springframework.web.client.RestClient.Builder} через
 * {@link RestClientCustomizer}: устанавливает таймауты подключения/чтения и перехватчик для
 * логирования HTTP-запросов. API-ключи маскируются в логах. Каждый сервис инжектирует
 * {@code RestClient.Builder} и строит собственный клиент с нужным baseUrl и заголовками.
 *
 * @author Stepan Kholodov
 */
@Configuration
public class AppConfig {

    @Value("${http.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${http.read-timeout-ms:10000}")
    private int readTimeoutMs;

    /**
     * Кастомизирует авто-конфигурируемый {@code RestClient.Builder}: добавляет таймауты и
     * перехватчик логирования, применяемые ко всем клиентам.
     */
    @Bean
    public RestClientCustomizer restClientCustomizer() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        return builder -> builder.requestFactory(factory).requestInterceptor(loggingInterceptor());
    }

    private ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            var log = LoggerFactory.getLogger("HTTP");
            String safeUri =
                    request.getURI().toString().replaceAll("key=[^&]+", "key=***");
            log.debug("=> {} {}", request.getMethod(), safeUri);
            var resp = execution.execute(request, body);
            log.info("<= {}", resp.getStatusCode());
            return resp;
        };
    }
}