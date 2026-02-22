package net.blackhacker.ares.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = true)
public class MediaTypeConverter implements AttributeConverter<MediaType, String> {

    @Override
    public String convertToDatabaseColumn(MediaType mediaType) {
        if (mediaType == null){
            return null;
        }
        return mediaType.toString();
    }

    @Override
    public MediaType convertToEntityAttribute(String s) {
        if  (s == null || s.isEmpty()){
            return null;
        }
        return MediaType.parseMediaType(s);
    }
}
