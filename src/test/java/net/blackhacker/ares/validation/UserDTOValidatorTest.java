package net.blackhacker.ares.validation;

import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = UserDTOValidator.class)
@ExtendWith(MockitoExtension.class)
class UserDTOValidatorTest {

    @MockitoBean
    private EmailValidator emailValidator;

    @MockitoBean
    private PasswordValidator passwordValidator;

    @MockitoBean
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
        when(userService.getUserByEmail(userDTO.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertDoesNotThrow(() -> userDTOValidator.validateUserForRegistration(userDTO));
    }
}
