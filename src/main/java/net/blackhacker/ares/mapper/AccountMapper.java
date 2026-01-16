package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.AccountDTO;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Component;

@Component
public class AccountMapper implements ModelDTOMapper<Account, AccountDTO>{

    private final PasswordEncoder passwordEncoder;

    public AccountMapper(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AccountDTO toDTO(Account account) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUsername(account.getUsername());
        accountDTO.setType(account.getType().toString());
        return accountDTO;
    }

    @Override
    public Account toModel(AccountDTO accountDTO) {
        Account account = new Account();
        account.setUsername(accountDTO.getUsername());
        account.setPassword(passwordEncoder.encode(accountDTO.getPassword()));
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
