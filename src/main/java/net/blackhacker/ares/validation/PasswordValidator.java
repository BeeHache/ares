package net.blackhacker.ares.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    @Value("${user.password.min_length}")
    private Integer minPasswordLength;

    private static final Pattern UC_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LC_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[@$!%*?&].*");

    public void validatePassword(String password) {
        if (password == null || password.length() < minPasswordLength) {
            throw new ValidationException("Password must be at least " + minPasswordLength + " characters long.");
        }
        if (!UC_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Password must contain at least one uppercase letter.");
        }
        if (!LC_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Password must contain at least one lowercase letter.");
        }
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Password must contain at least one digit.");
        }
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Password must contain at least one special character.");
        }
    }
}
