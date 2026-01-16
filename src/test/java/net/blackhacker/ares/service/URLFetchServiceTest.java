package net.blackhacker.ares.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class URLFetchServiceTest {

    private URLFetchService urlFetchService;

    @BeforeEach
    void setUp() {
        urlFetchService = new URLFetchService();
    }

    @Test
    void fetchImageBytes_shouldThrowServiceException_whenUrlIsInvalid() {
        String invalidUrl = "invalid-url";
        assertThrows(ServiceException.class, () -> urlFetchService.fetchImageBytes(invalidUrl));
    }

    @Test
    void getContentType_shouldThrowServiceException_whenUrlIsInvalid() {
        String invalidUrl = "invalid-url";
        assertThrows(ServiceException.class, () -> urlFetchService.getContentType(invalidUrl));
    }
}
