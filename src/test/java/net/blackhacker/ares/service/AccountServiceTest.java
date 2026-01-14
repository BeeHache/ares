package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = AccountService.class)
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @MockitoBean
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void findByToken_shouldReturnAccount_whenTokenExists() {
        // Arrange
        String token = "valid-token";
        Account account = new Account();
        account.setToken(token);
        when(accountRepository.findByToken(token)).thenReturn(Optional.of(account));

        // Act
        Optional<Account> result = accountService.findByToken(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(token, result.get().getToken());
        verify(accountRepository, times(1)).findByToken(token);
    }

    @Test
    void findByToken_shouldReturnEmpty_whenTokenDoesNotExist() {
        // Arrange
        String token = "invalid-token";
        when(accountRepository.findByToken(token)).thenReturn(Optional.empty());

        // Act
        Optional<Account> result = accountService.findByToken(token);

        // Assert
        assertTrue(result.isEmpty());
        verify(accountRepository, times(1)).findByToken(token);
    }

    @Test
    void findAccountByUsername_shouldReturnAccount_whenUsernameExists() {
        // Arrange
        String username = "testuser";
        Account account = new Account();
        account.setUsername(username);
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));

        // Act
        Optional<Account> result = accountService.findAccountByUsername(username);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(accountRepository, times(1)).findByUsername(username);
    }

    @Test
    void findAccountByUsername_shouldReturnEmpty_whenUsernameDoesNotExist() {
        // Arrange
        String username = "nonexistent";
        when(accountRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<Account> result = accountService.findAccountByUsername(username);

        // Assert
        assertTrue(result.isEmpty());
        verify(accountRepository, times(1)).findByUsername(username);
    }

    @Test
    void saveAccount_shouldReturnSavedAccount() {
        // Arrange
        Account account = new Account();
        account.setUsername("newuser");
        when(accountRepository.save(account)).thenReturn(account);

        // Act
        Account result = accountService.saveAccount(account);

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // Arrange
        String username = "testuser";
        Account account = new Account();
        account.setUsername(username);
        account.setPassword("password");
        // Account implements UserDetails, so we can return it directly
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));

        // Act
        UserDetails userDetails = accountService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        verify(accountRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {
        // Arrange
        String username = "nonexistent";
        when(accountRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> accountService.loadUserByUsername(username));
        verify(accountRepository, times(1)).findByUsername(username);
    }
}
