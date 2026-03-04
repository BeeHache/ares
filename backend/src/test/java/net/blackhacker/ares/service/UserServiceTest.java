package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.EmailConfirmationCode;
import net.blackhacker.ares.model.Feed;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.thymeleaf.TemplateEngine;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

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
    private CacheService cacheService;

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
        // Re-instantiate to ensure @Value is set (though @InjectMocks might handle it if we used constructor injection properly with mocks)
        // But for safety:
        userService = new UserService(userRepository, emailSenderService, emailConfirmationRepository, cacheService, transactionTemplate, "http://localhost:4200");

        existingEnabledAccount = new Account();
        existingEnabledAccount.setUsername("testuser");
        existingEnabledAccount.setAccountEnabledAt(ZonedDateTime.now().minusDays(1));

        existingNonenabledAccount = new  Account();
        existingNonenabledAccount.setUsername("testnonuser");

        nonExistingAccount = new Account();

        user = new User();
        user.setId(1L);
        user.setEmail(testEmail);
        user.setAccount(existingEnabledAccount);
        user.setFeeds(new HashSet<>());

        existingUser = new User();
        existingUser.setEmail(takenEmail);
        existingUser.setAccount(existingEnabledAccount);

        existingNonenabledUser =  new User();
        existingNonenabledUser.setEmail(takenEmail);
        existingNonenabledUser.setAccount(existingNonenabledAccount);

        ecc = new EmailConfirmationCode();
        ecc.setCode(UUID.randomUUID().toString());
        ecc.setEmail(testEmail);
    }

    @Test
    void registerUser_shouldSaveUser_whenEmailIsAvailable() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User registeredUser = userService.registerUser(user);

        assertNotNull(registeredUser);
        assertEquals(testEmail, registeredUser.getEmail());
        verify(userRepository).save(user);
        verify(emailSenderService).sendEmail(eq(testEmail), anyString(), anyString(), anyString(),
                                             anyString(), anyString(), anyString(), anyString());
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
        
        User registeredUser = userService.registerUser(existingNonenabledUser);

        assertNotNull(registeredUser);
        verify(userRepository, never()).save(existingNonenabledUser);
        verify(emailSenderService).sendEmail(anyString(), anyString(), anyString(), anyString(),
                                             anyString(),anyString(), anyString(), anyString());
    }

    @Test
    void recoverUser_shouldSendEmail_whenUserExists() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));
        
        userService.recoverUser(testEmail);
        
        verify(emailSenderService).sendEmail(eq(testEmail), anyString(), anyString(), eq("email-recovery"),
                                                            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void recoverUser_shouldDoNothing_whenUserDoesNotExist() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        
        userService.recoverUser(testEmail);
        
        verify(emailSenderService, never()).sendEmail(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void confirm_shouldEnableAccount_whenCodeIsValid() {
        when(emailConfirmationRepository.findById(ecc.getCode())).thenReturn(Optional.of(ecc));
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));
        
        boolean result = userService.confirm(ecc.getCode());
        
        assertTrue(result);
        assertNotNull(user.getAccount().getAccountEnabledAt());
        verify(userRepository).save(user);
        verify(emailConfirmationRepository).delete(ecc);
    }

    @Test
    void confirm_shouldReturnFalse_whenCodeIsInvalid() {
        when(emailConfirmationRepository.findById("invalid")).thenReturn(Optional.empty());
        
        boolean result = userService.confirm("invalid");
        
        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void subscribeUserToFeed_shouldAddFeedAndSave() {
        Feed feed = new Feed();
        
        // Mock TransactionTemplate
        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.subscribeUserToFeed(user, feed);
        
        assertTrue(user.getFeeds().contains(feed));
        verify(userRepository).save(user);
    }

    @Test
    void cancelUser_shouldCallRepository() {

        // Mock TransactionTemplate
        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        when(userRepository.save(user)).thenReturn(user);
        userService.cancelUser(user);
        assertFalse(user.getAccount().isAccountNonExpired());
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
}
