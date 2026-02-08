package net.blackhacker.ares.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class DateTimeReformatter {
    static final private DateTimeFormatter outputFormat = DateTimeFormatter.ISO_INSTANT;
    static final private DateTimeFormatter[] inputFormats = {
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz"),
            DateTimeFormatter.RFC_1123_DATE_TIME,
    };

    static public String reformat(String input) {
    String output = "";
        for (DateTimeFormatter formatter : inputFormats) {
            try {
                output =  outputFormat.format(formatter.parse(input));
                break;
            } catch (Exception e) {
                //
            }
        }
        if  (output.isEmpty()) {
            log.error("DateTimeReformatter: Couldn't reformat date: {}", input);
        }
        return output;
    }
}