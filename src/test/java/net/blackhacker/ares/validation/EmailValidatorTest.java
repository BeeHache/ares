package net.blackhacker.ares.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class EmailValidatorTest {

    @InjectMocks
    private EmailValidator emailValidator;

    @BeforeEach
    void setUp() {
        emailValidator = new EmailValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test@example.com",
            "test.name@example.co.uk",
            "test_name+alias@example.com"
    })
    void validateEmail_shouldPass_forValidEmails(String email) {
        assertDoesNotThrow(() -> emailValidator.validateEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plainaddress",
            "@missingusername.com",
            "username@.com",
            "username@.com.",
            "username@com",
            " "
    })
    void validateEmail_shouldThrowException_forInvalidEmails(String email) {
        assertThrows(ValidationException.class, () -> emailValidator.validateEmail(email));
    }

    @Test
    void validateEmail_shouldThrowException_forNullEmail() {
        assertThrows(ValidationException.class, () -> emailValidator.validateEmail(null));
    }
}
