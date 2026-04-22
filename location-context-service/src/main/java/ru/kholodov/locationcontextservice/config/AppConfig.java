package ru.kholodov.locationcontextservice.config;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {



    @Bean
    public RestTemplate restTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.getInterceptors().add((request, body, execution) -> {
            var log = LoggerFactory.getLogger("HTTP");
            String safeUri = request.getURI().toString().replaceAll("key=[^&]+", "key=***");
            log.debug("=> {} {}", request.getMethod(), safeUri);
            request.getHeaders().forEach((k, v) -> log.info("=>   {}: {}", k, v));

            ClientHttpResponse resp = execution.execute(request, body);

            log.info("<= {} {}", resp.getStatusCode(), resp.getStatusText());
            resp.getHeaders().forEach((k, v) -> log.info("<=   {}: {}", k, v));
            return resp;
        });
        return rt;
    }
}