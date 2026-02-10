package net.blackhacker.ares.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class URLFetchServiceTest {

    @Mock
    private RestClient restClient;

    @InjectMocks
    private URLFetchService urlFetchService;

    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.cookies(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void fetchString_shouldReturnResponseEntity() {
        String url = "https://example.com";
        String expectedBody = "response body";
        ResponseEntity<String> expectedResponse = ResponseEntity.ok(expectedBody);

        when(responseSpec.toEntity(String.class)).thenReturn(expectedResponse);

        ResponseEntity<String> result = urlFetchService.fetchString(url);

        assertNotNull(result);
        assertEquals(expectedBody, result.getBody());
    }

    @Test
    void fetchString_withHeadersAndCookies_shouldReturnResponseEntity() {
        String url = "https://example.com";
        Map<String, String> headers = new HashMap<>();
        headers.put("Header", "Value");
        Map<String, String> cookies = new HashMap<>();
        cookies.put("Cookie", "Value");

        ResponseEntity<String> expectedResponse = ResponseEntity.ok("body");
        when(responseSpec.toEntity(String.class)).thenReturn(expectedResponse);

        ResponseEntity<String> result = urlFetchService.fetchString(url, headers, cookies);

        assertNotNull(result);
        assertEquals("body", result.getBody());
    }

    @Test
    void fetchBytes_shouldReturnResponseEntity() {
        String url = "https://example.com/image.png";
        byte[] expectedBody = new byte[]{1, 2, 3};
        ResponseEntity<byte[]> expectedResponse = ResponseEntity.ok(expectedBody);

        when(responseSpec.toEntity(byte[].class)).thenReturn(expectedResponse);

        ResponseEntity<byte[]> result = urlFetchService.fetchBytes(url);

        assertNotNull(result);
        assertEquals(expectedBody, result.getBody());
    }
}
