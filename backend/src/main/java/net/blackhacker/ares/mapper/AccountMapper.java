package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.AccountDTO;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.User;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class AccountMapper implements ModelDTOMapper<Account, AccountDTO>{

    private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;

    public AccountMapper(ObjectProvider<PasswordEncoder> passwordEncoderProvider){
        this.passwordEncoderProvider = passwordEncoderProvider;
    }

    @Override
    public AccountDTO toDTO(Account account) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(account.getId());
        accountDTO.setUsername(account.getUsername());
        accountDTO.setType(account.getType().toString());
        if (account.getAccountExpiresAt() != null) {
            accountDTO.setAccountExpiresAt(account.getAccountExpiresAt().format(DateTimeFormatter.ISO_INSTANT));
        }
        if (account.getPasswordExpiresAt() != null) {
            accountDTO.setPasswordExpiresAt(account.getPasswordExpiresAt().format(DateTimeFormatter.ISO_INSTANT));
        }
        if (account.getAccountLockedUntil() != null) {
            accountDTO.setAccountLockedUntil(account.getAccountLockedUntil().format(DateTimeFormatter.ISO_INSTANT));
        }
        if (account.getAccountEnabledAt() != null) {
            accountDTO.setAccountEnabledAt(account.getAccountEnabledAt().format(DateTimeFormatter.ISO_INSTANT));
        }
        return accountDTO;
    }

    @Override
    public Account toModel(AccountDTO accountDTO) {
        Account account = new Account();
        account.setUsername(accountDTO.getUsername());
        account.setPassword(passwordEncoderProvider.getObject().encode(accountDTO.getPassword()));
        account.setType(Account.AccountType.valueOf(accountDTO.getType()));
        return account;
    }

    public User toUser(AccountDTO accountDTO){
        User user = new User();
        user.setEmail(accountDTO.getUsername());
        user.setAccount(toModel(accountDTO));
        return user;
    }
}
