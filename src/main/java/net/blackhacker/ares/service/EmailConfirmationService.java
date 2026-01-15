package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.EmailConfirmationCode;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.EmailConfirmationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailConfirmationService {

    private final AccountService accountService;
    private final UserService userService;
    private final EmailConfirmationRepository emailConfirmationRepository;

    public EmailConfirmationService(
            AccountService accountService,
            UserService userService,
            EmailConfirmationRepository emailConfirmationRepository) {
        this.accountService = accountService;
        this.userService = userService;
        this.emailConfirmationRepository = emailConfirmationRepository;
    }


    public boolean confirmEmail(String code) {

        Optional<EmailConfirmationCode> optionalCode = emailConfirmationRepository.findById(code);
        if (optionalCode.isEmpty()){
            return false;
        }

        EmailConfirmationCode confirmationCode = optionalCode.get();
        User user = userService.getUserByEmail(confirmationCode.getEmail());
        if (user == null){
            return false;
        }

        Account account = user.getAccount();
        account.setAccountEnabledAt(LocalDateTime.now());
        accountService.saveAccount(account);

        return true;
    }

}
