package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.EmailConfirmationRepository;
import net.blackhacker.ares.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.TemplateEngine;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = UserService.class)
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EmailConfirmationRepository emailConfirmationRepository;

    @MockitoBean
    private EmailSenderService emailSenderService;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private TemplateEngine templateEngine;

    @InjectMocks
    private UserService userService;

    private User user, existingUser;
    private Account account, nonExistingAccount;
    private String testEmail = "test@example.com";
    private String takenEmail = "taken@example.com";

    @BeforeEach
    public void setUP() {

        account = new Account();
        account.setUsername("testuser");

        nonExistingAccount = new Account();

        user = new User();
        user.setEmail(testEmail);
        user.setAccount(account);

        existingUser = new User();
        existingUser.setEmail(takenEmail);
        existingUser.setAccount(account);

        reset(userRepository);
    }

    @Test
    void registerUser_shouldSaveUser_whenEmailIsAvailable() {
        // Arrange

        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        // Act
        Optional<User> registeredUser = userService.registerUser(user);

        // Assert
        assertTrue(registeredUser.isPresent());
        assertEquals(testEmail, registeredUser.get().getEmail());
        verify(userRepository).existsByEmail(testEmail);
        verify(userRepository).save(user);
    }

    @Test
    void registerUser_shouldReturnNull_whenEmailIsTaken() {

        when(userRepository.existsByEmail(takenEmail)).thenReturn(true);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        Optional<User> registeredUser = userService.registerUser(existingUser);
        assertFalse(registeredUser.isPresent());
        verify(userRepository).existsByEmail(takenEmail);
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
        when(userRepository.findByAccount(account)).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUserByAccount(account);
        assertTrue(result.isPresent());
        assertEquals(account, result.get().getAccount());
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
        Optional<User> result = userService.getUserByAccount(account);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }
}
