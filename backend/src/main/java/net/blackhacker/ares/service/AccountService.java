package net.blackhacker.ares.service;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.repository.jpa.AccountRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

@Slf4j
@Service
public class AccountService implements UserDetailsService {
    final private AccountRepository accountRepository;
    final private Long refreshTokenDurationMs;
    final private EmailSenderService emailSenderService;

    public AccountService(AccountRepository accountRepository,
                          EmailSenderService emailSenderService,
                          @Value("${security.jwt.refresh_expiration_ms: 86400000}") Long refreshTokenDurationMs) {
        this.accountRepository = accountRepository;
        this.refreshTokenDurationMs = refreshTokenDurationMs;
        this.emailSenderService = emailSenderService;
    }

    public Optional<Account> getAccount(Long id) {
        return accountRepository.findById(id);
    }

    public Page<Account> getAccounts(Pageable pageable) {
        return accountRepository.findAllAccountProjection(pageable);
    }

    public Optional<Account> findAccountByUsername(String username){
        return accountRepository.findByUsername(username);
    }

    public Account saveAccount(Account account){
        return accountRepository.save(account);
    }

    @Transactional
    public void loginFailed(String username) {
        accountRepository.findByUsername(username).ifPresent(account -> {
            int attempts = account.getFailedLoginAttempts() == null ? 0 : account.getFailedLoginAttempts();
            attempts++;
            account.setFailedLoginAttempts(attempts);
            
            if (attempts > 3) {
                log.warn("Account {} locked due to too many failed login attempts", username);
                account.setAccountLockedUntil(ZonedDateTime.now().plusMinutes(15));
                emailSenderService.sendAccountLockedEmail(username);
            }
            
            accountRepository.save(account);
        });
    }

    @Transactional
    public void loginSucceeded(String username) {
        accountRepository.findByUsername(username).ifPresent(account -> {
            if (account.getFailedLoginAttempts() != null && account.getFailedLoginAttempts() > 0) {
                account.setFailedLoginAttempts(0);
                accountRepository.save(account);
            }
            
            account.setLastLogin(ZonedDateTime.now());
            accountRepository.save(account);
        });
    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return  accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User '"+username+"'not found."));
    }
}
