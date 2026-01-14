package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest(classes = UserService.class)
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private Account account;

    @BeforeEach
    public void setUP() {

        account = new Account();
        account.setUsername("testuser");

        user = new User();
        user.setEmail("test@example.com");
        user.setAccount(account);
    }

    @Test
    void registerUser_shouldSaveUser_whenEmailIsAvailable() {
        // Arrange

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User registeredUser = userService.registerUser(user);

        // Assert
        assertNotNull(registeredUser);
        assertEquals(user.getEmail(), registeredUser.getEmail());
        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void registerUser_shouldReturnNull_whenEmailIsTaken() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);
        User registeredUser = userService.registerUser(user);
        assertNull(registeredUser);
    }

    @Test
    void getUserByEmail_shouldReturnUser_whenUserExists() {

        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void getUserByEmail_shouldReturnNull_whenUserDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        User result = userService.getUserByEmail(email);

        // Assert
        assertNull(result);
    }

    @Test
    void getUserByAccount_shouldReturnUser_whenUserExists() {
        when(userRepository.findByAccount(account)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserByAccount(account);

        // Assert
        assertNotNull(result);
        assertEquals(account, result.getAccount());
    }

    @Test
    void getUserByAccount_shouldReturnNull_whenUserDoesNotExist() {
        // Arrange
        Account account = new Account();
        when(userRepository.findByAccount(account)).thenReturn(Optional.empty());

        // Act
        User result = userService.getUserByAccount(account);

        // Assert
        assertNull(result);
    }

    @Test
    void saveUser_shouldCallRepositorySave() {
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User result = userService.saveUser(user);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(user);
    }
}
