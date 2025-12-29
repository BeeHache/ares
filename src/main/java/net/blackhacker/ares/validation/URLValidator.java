package net.blackhacker.ares.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class URLValidator {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    );

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

        if (!URL_PATTERN.matcher(url).matches()) {
            throw new ValidationException("Invalid URL format: " + url);
        }
    }
}
