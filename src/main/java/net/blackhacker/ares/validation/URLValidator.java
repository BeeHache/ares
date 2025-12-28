package net.blackhacker.ares.validation;

import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Component
public class URLValidator {

    /**
     * Validates the given URL string and throws a ValidationException if it's invalid.
     *
     * @param url The string to validate.
     * @throws ValidationException if the URL is null, empty, or has an invalid format.
     */
    public void validateURL(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new ValidationException("URL must not be empty.");
        }

        try {
            // Using toURI() is a stricter check that validates the URL syntax more thoroughly.
            new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new ValidationException("Invalid URL format: " + url, e);
        }
    }
}
