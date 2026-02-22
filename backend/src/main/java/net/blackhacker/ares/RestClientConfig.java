package net.blackhacker.ares;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List; // Added missing import

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient customRestClient(RestClient.Builder builder) { // Changed to RestClient.Builder
        return builder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeaders(headers -> {
                    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                    headers.set("X-API-Version", "1.0");
                })
                .build();
    }
}
