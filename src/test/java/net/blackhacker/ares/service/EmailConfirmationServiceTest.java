package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.EmailConfirmationCode;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.EmailConfirmationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = EmailConfirmationService.class)
@ExtendWith(MockitoExtension.class)
class EmailConfirmationServiceTest {

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EmailConfirmationRepository emailConfirmationRepository;

    @InjectMocks
    private EmailConfirmationService emailConfirmationService;

    @Test
    void confirmEmail_shouldReturnTrue_whenCodeIsValid() {
        // Arrange
        String code = "valid-code";
        String email = "test@example.com";
        
        EmailConfirmationCode confirmationCode = new EmailConfirmationCode();
        confirmationCode.setCode(code);
        confirmationCode.setEmail(email);

        User user = new User();
        user.setEmail(email);
        Account account = new Account();
        user.setAccount(account);

        when(emailConfirmationRepository.findById(code)).thenReturn(Optional.of(confirmationCode));
        when(userService.getUserByEmail(email)).thenReturn(user);

        // Act
        boolean result = emailConfirmationService.confirmEmail(code);

        // Assert
        assertTrue(result);
        verify(accountService).saveAccount(account);
        assertNotNull(account.getAccountEnabledAt());
    }

    @Test
    void confirmEmail_shouldReturnFalse_whenCodeIsInvalid() {
        // Arrange
        String code = "invalid-code";
        when(emailConfirmationRepository.findById(code)).thenReturn(Optional.empty());

        // Act
        boolean result = emailConfirmationService.confirmEmail(code);

        // Assert
        assertFalse(result);
        verify(accountService, never()).saveAccount(any());
    }

    @Test
    void confirmEmail_shouldReturnFalse_whenUserNotFound() {
        // Arrange
        String code = "valid-code";
        String email = "test@example.com";

        EmailConfirmationCode confirmationCode = new EmailConfirmationCode();
        confirmationCode.setCode(code);
        confirmationCode.setEmail(email);

        when(emailConfirmationRepository.findById(code)).thenReturn(Optional.of(confirmationCode));
        when(userService.getUserByEmail(email)).thenReturn(null);

        // Act
        boolean result = emailConfirmationService.confirmEmail(code);

        // Assert
        assertFalse(result);
        verify(accountService, never()).saveAccount(any());
    }
}
