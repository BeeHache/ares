package net.blackhacker.ares.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class URLValidatorTest {

    private URLValidator urlValidator;

    @BeforeEach
    void setUp() {
        urlValidator = new URLValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com",
            "https://example.com/path?query=value",
            "ftp://user:password@example.com"
    })
    void validateURL_shouldPass_forValidUrls(String url) {
        assertDoesNotThrow(() -> urlValidator.validateURL(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-a-url",
            "http//example.com",
            "example.com",
            " "
    })
    void validateURL_shouldThrowException_forInvalidUrls(String url) {
        assertThrows(ValidationException.class, () -> urlValidator.validateURL(url));
    }

    @Test
    void validateURL_shouldThrowException_forNullUrl() {
        assertThrows(ValidationException.class, () -> urlValidator.validateURL(null));
    }
}
