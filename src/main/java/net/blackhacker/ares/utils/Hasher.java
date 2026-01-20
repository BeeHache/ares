package net.blackhacker.ares.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
@Component
public class Hasher {

    final static int MAX_LENGTH = 43;

    public  String hash(String x){
        return hash(x, Hasher.MAX_LENGTH);
    }

    public String hash(String x, int length){
        try {
            if (length > Hasher.MAX_LENGTH) length = Hasher.MAX_LENGTH;
            MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(x.getBytes(StandardCharsets.UTF_8));
            String base64Encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
            return base64Encoded.substring(0, length);
        } catch (Exception e) {
            log.error("Error hashing string", e);
            return "";
        }
    }
}
