package net.blackhacker.ares.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultipartFileValidatorTest {

    private MultipartFileValidator multipartFileValidator;

    @BeforeEach
    void setUp() {
        multipartFileValidator = new MultipartFileValidator();
    }

    @Test
    void validateMultipartFile_shouldPass_forValidFile() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "some content".getBytes());

        // Act & Assert
        assertDoesNotThrow(() -> multipartFileValidator.validateMultipartFile(file));
    }

    @Test
    void validateMultipartFile_shouldThrowException_forNullFile() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> multipartFileValidator.validateMultipartFile(null));
    }

    @Test
    void validateMultipartFile_shouldThrowException_forEmptyFile() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        // Act & Assert
        assertThrows(ValidationException.class, () -> multipartFileValidator.validateMultipartFile(file));
    }
}
