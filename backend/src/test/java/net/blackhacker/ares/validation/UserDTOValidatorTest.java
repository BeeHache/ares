package net.blackhacker.ares.validation;

import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDTOValidatorTest {

    @Mock
    private EmailValidator emailValidator;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserDTOValidator userDTOValidator;

    @Test
    void validateUserForRegistration_shouldPass_whenDataIsValidAndUserDoesNotExist() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("new@example.com");
        userDTO.setPassword("ValidPass123!");

        doNothing().when(emailValidator).validateEmail(userDTO.getEmail());
        doNothing().when(passwordValidator).validatePassword(userDTO.getPassword());

        // Act & Assert
        assertDoesNotThrow(() -> userDTOValidator.validateUserForRegistration(userDTO));
    }
}
