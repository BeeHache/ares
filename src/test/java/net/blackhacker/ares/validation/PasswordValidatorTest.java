package net.blackhacker.ares.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PasswordValidatorTest {

    private PasswordValidator passwordValidator;

    @BeforeEach
    void setUp() {
        passwordValidator = new PasswordValidator();
        // Manually set the value that would normally be injected by @Value
        ReflectionTestUtils.setField(passwordValidator, "minPasswordLength", 8);
    }

    @Test
    void validatePassword_shouldPass_forValidPassword() {
        assertDoesNotThrow(() -> passwordValidator.validatePassword("ValidPass1!"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short",           // Too short
            "nouppercase1!",   // No uppercase
            "NOLOWERCASE1!",   // No lowercase
            "NoDigit!!",       // No digit
            "NoSpecial123"     // No special character
    })
    void validatePassword_shouldThrowException_forInvalidPasswords(String password) {
        assertThrows(ValidationException.class, () -> passwordValidator.validatePassword(password));
    }

    @Test
    void validatePassword_shouldThrowException_forNullPassword() {
        assertThrows(ValidationException.class, () -> passwordValidator.validatePassword(null));
    }
}
