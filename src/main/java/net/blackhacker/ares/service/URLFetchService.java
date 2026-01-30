package net.blackhacker.ares.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class URLFetchService {

    final private RestClient restClient;

    public URLFetchService(RestClient restClient) {
        this.restClient = restClient;
    }

    public ResponseEntity<String> fetchString(String urlString){
        return fetchString(urlString,null,null);
    }

    public ResponseEntity<String> fetchString(String urlString,Map<String,String> headers){
        return fetchString(urlString,headers,null);
    }

    public ResponseEntity<String> fetchString(String urlString,
                                              Map<String, String> headersMap,
                                              Map<String, String> cookiesMap) {

        final Map<String, String> h = headersMap==null?new HashMap<>():headersMap;
        final Map<String, String> c = cookiesMap==null?new HashMap<>():cookiesMap;

        return restClient.get()
                .uri(urlString)
                .headers(httpHeaders -> {
                    h.forEach(httpHeaders::add);
                })
                .cookies(httpCookies -> {
                    c.forEach(httpCookies::add);
                })
                .retrieve()
                .toEntity(String.class);
    }


    public ResponseEntity<byte[]> fetchBytes(String urlString){
        return fetchBytes(urlString,null,null);
    }

    public ResponseEntity<byte[]> fetchBytes(String urlString,Map<String,String> headers){
        return fetchBytes(urlString,headers,null);
    }

    public ResponseEntity<byte[]> fetchBytes(String urlString,
                                             Map<String, String> headersMap,
                                             Map<String, String> cookiesMap) {
        final Map<String, String> h = headersMap==null?new HashMap<>():headersMap;
        final Map<String, String> c = cookiesMap==null?new HashMap<>():cookiesMap;

        return restClient.get()
                .uri(urlString)
                .headers(httpHeaders -> {
                    h.forEach(httpHeaders::add);
                })
                .cookies(httpCookies -> {
                    c.forEach(httpCookies::add);
                })
                .retrieve()
                .toEntity(byte[].class);

    }
}
