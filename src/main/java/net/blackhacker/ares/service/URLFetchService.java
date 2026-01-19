package net.blackhacker.ares.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class URLFetchService {
    public ResponseEntity<String> fetchString(String urlString) {
        RestClient restClient = RestClient.create();

        return restClient.get()
                .uri(urlString)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<byte[]> fetchBytes(String urlString) {
        RestClient restClient = RestClient.create();

        return restClient.get()
                .uri(urlString)
                .retrieve()
                .toEntity(byte[].class);

    }


}
