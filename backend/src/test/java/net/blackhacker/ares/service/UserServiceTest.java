package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.EmailConfirmationCode;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.crud.EmailConfirmationRepository;
import net.blackhacker.ares.repository.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.support.TransactionTemplate;
import org.thymeleaf.TemplateEngine;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailConfirmationRepository emailConfirmationRepository;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private CacheService cacheService; // Added Mock

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private UserService userService;

    private User user, existingUser, existingNonenabledUser;
    private Account existingEnabledAccount, existingNonenabledAccount, nonExistingAccount;
    private String testEmail = "test@example.com";
    private String takenEmail = "taken@example.com";
    private EmailConfirmationCode ecc;

    @BeforeEach
    public void setUP() {

        existingEnabledAccount = new Account();
        existingEnabledAccount.setUsername("testuser");
        existingEnabledAccount.setAccountEnabledAt(ZonedDateTime.now().minusDays(1));

        existingNonenabledAccount = new  Account();
        existingNonenabledAccount.setUsername("testnonuser");

        nonExistingAccount = new Account();

        user = new User();
        user.setEmail(testEmail);
        user.setAccount(existingEnabledAccount);

        existingUser = new User();
        existingUser.setEmail(takenEmail);
        existingUser.setAccount(existingEnabledAccount);

        existingNonenabledUser =  new User();
        existingNonenabledUser.setEmail(takenEmail);
        existingNonenabledUser.setAccount(existingNonenabledAccount);

        reset(userRepository);

        ecc = new EmailConfirmationCode();
        ecc.setCode(UUID.randomUUID().toString());


    }

    @Test
    void registerUser_shouldSaveUser_whenEmailIsAvailable() {
        // Arrange
        
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User registeredUser = userService.registerUser(user);

        // Assert
        assertNotNull(registeredUser);
        assertEquals(testEmail, registeredUser.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void registerUser_shouldThrowException_whenEmailIsTakenAndEnabled() {
        when(userRepository.findByEmail(existingUser.getEmail())).thenReturn(Optional.of(existingUser));
        assertThrows(ServiceException.class, () -> {
            userService.registerUser(existingUser);
        });
    }

    @Test
    void registerUser_shouldReturnUser_whenEmailIsTakenButNotEnabled() {

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingNonenabledUser));
        when(emailConfirmationRepository.save(any(EmailConfirmationCode.class))).thenReturn(ecc);
        doNothing().when(emailSenderService).sendEmail(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString());

        User registeredUser = userService.registerUser(user);

        assertNotNull(registeredUser);
        verify(userRepository, never()).save(user);
    }

    @Test
    void getUserByEmail_shouldReturnUser_whenUserExists() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUserByEmail(testEmail);
        assertTrue(result.isPresent());
        assertEquals(testEmail, result.get().getEmail());
    }

    @Test
    void getUserByEmail_shouldReturnNull_whenUserDoesNotExist() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        Optional<User> result = userService.getUserByEmail(email);
        assertFalse(result.isPresent());
    }

    @Test
    void getUserByAccount_shouldReturnUser_whenUserExists() {
        when(userRepository.findByAccount(existingEnabledAccount)).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUserByAccount(existingEnabledAccount);
        assertTrue(result.isPresent());
        assertEquals(existingEnabledAccount, result.get().getAccount());
    }

    @Test
    void getUserByAccount_shouldReturnNull_whenUserDoesNotExist() {
        when(userRepository.findByAccount(nonExistingAccount)).thenReturn(Optional.empty());
        Optional<User> result = userService.getUserByAccount(nonExistingAccount);
        assertFalse(result.isPresent());
    }

    @Test
    void saveUser_shouldCallRepositorySave() {
        when(userRepository.save(user)).thenReturn(user);
        User result = userService.saveUser(user);
        assertNotNull(result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void getUserByAccount_shouldReturnAccount_whenAccountExists() {
        when(userRepository.findByAccount(any(Account.class))).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUserByAccount(existingEnabledAccount);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }
}
