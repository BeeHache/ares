package net.blackhacker.ares.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URL;

@Slf4j
@Converter(autoApply = true)
@Component
public class URLConverter implements AttributeConverter<URL, String> {

    @Override
    public String convertToDatabaseColumn(URL url) {
        if (url == null) {
            return null;
        }
        return url.toString();
    }

    @Override
    public URL convertToEntityAttribute(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return new URI(s).toURL();
        } catch (Exception e) {
            log.error(String.format("URLConverter: Could not convert database string '%s' to URL object", s), e);
            return null;
        }
    }
}

