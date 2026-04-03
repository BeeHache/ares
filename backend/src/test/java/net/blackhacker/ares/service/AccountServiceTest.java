package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Admins;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.projection.AccountProjection;
import net.blackhacker.ares.repository.jpa.AccountRepository;
import net.blackhacker.ares.repository.jpa.AdminsRepository;
import net.blackhacker.ares.repository.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AdminsRepository adminsRepository;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private UserRepository userRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, adminsRepository, userRepository, emailSenderService, 3600000L);
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
    void createAccount_shouldCreateAdmin_whenTypeIsAdmin() {
        Account account = new Account();
        account.setUsername("admin@test.com");
        account.setType(AccountProjection.AccountType.ADMIN);

        when(accountRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.createAccount(account, "Test Admin");

        assertNotNull(result);
        verify(adminsRepository).save(any(Admins.class));
        verify(accountRepository).save(account);
    }

    @Test
    void createAccount_shouldCreateUser_whenTypeIsUser() {
        Account account = new Account();
        account.setUsername("user@test.com");
        account.setType(AccountProjection.AccountType.USER);

        when(accountRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.createAccount(account);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(account);
    }

    @Test
    void createAccount_shouldThrowConflict_whenUsernameExists() {
        Account account = new Account();
        account.setUsername("existing@test.com");
        when(accountRepository.findByUsername("existing@test.com")).thenReturn(Optional.of(new Account()));

        assertThrows(ResponseStatusException.class, () -> accountService.createAccount(account));
    }

    @Test
    void getAccountsFiltered_shouldReturnFilteredPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Account> page = new PageImpl<>(Collections.emptyList());
        
        when(accountRepository.findAllWithFilters(any(), any(), eq(pageable))).thenReturn(page);

        Page<Account> result = accountService.getAccountsFiltered(Account.AccountType.USER, true, pageable);

        assertNotNull(result);
        verify(accountRepository).findAllWithFilters(Account.AccountType.USER, true, pageable);
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
