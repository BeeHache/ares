package net.blackhacker.ares.service;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Admins;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.projection.AccountProjection;
import net.blackhacker.ares.repository.jpa.AccountRepository;
import net.blackhacker.ares.repository.jpa.AdminsRepository;
import net.blackhacker.ares.repository.jpa.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.Optional;

@Slf4j
@Service
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final AdminsRepository adminsRepository;
    private final UserRepository userRepository;
    private final Long refreshTokenDurationMs;
    private final EmailSenderService emailSenderService;

    public AccountService(AccountRepository accountRepository,
                          AdminsRepository adminsRepository,
                          UserRepository userRepository,
                          EmailSenderService emailSenderService,
                          @Value("${security.jwt.refresh_expiration_ms: 86400000}") Long refreshTokenDurationMs) {
        this.accountRepository = accountRepository;
        this.adminsRepository = adminsRepository;
        this.userRepository = userRepository;
        this.refreshTokenDurationMs = refreshTokenDurationMs;
        this.emailSenderService = emailSenderService;
    }

    public Optional<Account> getAccount(Long id) {
        return accountRepository.findById(id);
    }

    public Page<Account> getAccounts(Pageable pageable) {
        return accountRepository.findAllAccountProjection(pageable);
    }

    public Page<Account> getAccountsFiltered(Account.AccountType type, Boolean locked, Pageable pageable) {
        return accountRepository.findAllWithFilters(type, locked, pageable);
    }

    public Optional<Account> findAccountByUsername(String username){
        return accountRepository.findByUsername(username);
    }

    @Transactional
    public Account createAccount(Account account) {
        return createAccount(account, null);
    }

    @Transactional
    public Account createAccount(Account account, String adminName) {
        if (accountRepository.findByUsername(account.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account with username " + account.getUsername() + " already exists.");
        }

        // Ensure it's enabled by default when created by an Admin
        if (account.getAccountEnabledAt() == null) {
            account.setAccountEnabledAt(ZonedDateTime.now());
        }

        Account savedAccount = accountRepository.save(account);

        if (savedAccount.getType() == AccountProjection.AccountType.ADMIN) {
            Admins admin = new Admins();
            admin.setEmail(savedAccount.getUsername());
            admin.setName(adminName != null ? adminName : savedAccount.getUsername());
            admin.setAccount(savedAccount);
            adminsRepository.save(admin);
        } else {
            User user = new User();
            user.setEmail(savedAccount.getUsername());
            user.setAccount(savedAccount);
            userRepository.save(user);
        }

        return savedAccount;
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
