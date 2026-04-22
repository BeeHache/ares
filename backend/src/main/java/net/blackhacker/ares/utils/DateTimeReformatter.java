package net.blackhacker.ares.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

@Slf4j
public class DateTimeReformatter {
    static final private DateTimeFormatter outputFormat = DateTimeFormatter.ISO_INSTANT;
    
    static final private DateTimeFormatter[] inputFormats = {
            // Flexible RSS 2.0 format (handles single digit days and common timezones)
            new DateTimeFormatterBuilder()
                    .appendPattern("[EEE, ]d MMM yyyy HH:mm:ss [zzz][z][Z]")
                    .toFormatter(Locale.ENGLISH),
            
            // Standard ISO (for Atom)
            DateTimeFormatter.ISO_DATE_TIME,
            
            // RFC 1123
            DateTimeFormatter.RFC_1123_DATE_TIME,

            // fallback without day of week
            DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
    };

    static public String reformat(String input) {
        if (input == null || input.isBlank()) return "";
        
        String output = "";
        for (DateTimeFormatter formatter : inputFormats) {
            try {
                ZonedDateTime dt = ZonedDateTime.parse(input, formatter);
                output = outputFormat.format(dt);
                break;
            } catch (Exception e) {
                // Try next
            }
        }
        if (output.isEmpty()) {
            log.error("DateTimeReformatter: Couldn't reformat date: {}", input);
        }
        return output;
    }

    static public ZonedDateTime parse(String input) {
        if (input == null || input.isBlank()) return null;
        
        for (DateTimeFormatter format : inputFormats) {
            try {
                return ZonedDateTime.parse(input, format);
            } catch (Exception e) {
                // ignore and fall through
            }
        }

        log.error("DateTimeReformatter: Couldn't reformat date: {}", input);
        return null;
    }
}
