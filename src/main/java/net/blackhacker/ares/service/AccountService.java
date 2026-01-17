package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.repository.AccountRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService implements UserDetailsService {
    @Value("${security.jwt.refresh_expiration_ms: 86400000}") // default 24 hrs
    private Long refreshTokenDurationMs;

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Optional<Account> findAccountByUsername(String username){
        return accountRepository.findByUsername(username);
    }

    public Account saveAccount(Account account){
        return accountRepository.save(account);
    }


    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return  accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User '"+username+"'not found."));
    }
}
