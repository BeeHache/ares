package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.repository.jpa.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EmailSenderService emailSenderService;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, emailSenderService, 3600000L);
    }

    @Test
    void findAccountByUsername_shouldReturnAccount_whenUsernameExists() {
        String username = "testuser";
        Account account = new Account();
        account.setUsername(username);
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.findAccountByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
    }

    @Test
    void loginFailed_shouldIncrementAttempts() {
        String username = "testuser";
        Account account = new Account();
        account.setUsername(username);
        account.setFailedLoginAttempts(1);
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));

        accountService.loginFailed(username);

        assertEquals(2, account.getFailedLoginAttempts());
        verify(accountRepository).save(account);
    }

    @Test
    void loginFailed_shouldLockAccount_whenThresholdReached() {
        String username = "testuser";
        Account account = new Account();
        account.setUsername(username);
        account.setFailedLoginAttempts(3);
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));

        accountService.loginFailed(username);

        assertEquals(4, account.getFailedLoginAttempts());
        assertNotNull(account.getAccountLockedUntil());
        assertTrue(account.getAccountLockedUntil().isAfter(ZonedDateTime.now()));
        verify(emailSenderService).sendAccountLockedEmail(username);
        verify(accountRepository).save(account);
    }

    @Test
    void loginSucceeded_shouldResetAttempts() {
        String username = "testuser";
        Account account = new Account();
        account.setUsername(username);
        account.setFailedLoginAttempts(5);
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));

        accountService.loginSucceeded(username);

        assertEquals(0, account.getFailedLoginAttempts());
        assertNotNull(account.getLastLogin());
        verify(accountRepository).save(account);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        String username = "testuser";
        Account account = new Account();
        account.setUsername(username);
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));

        UserDetails userDetails = accountService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {
        String username = "nonexistent";
        when(accountRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> accountService.loadUserByUsername(username));
    }
}
