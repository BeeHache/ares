package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.EmailConfirmationCode;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.EmailConfirmationRepository;
import net.blackhacker.ares.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailSenderService emailSenderService;
    private final EmailConfirmationRepository emailConfirmationRepository;


    public UserService(UserRepository userRepository, EmailSenderService emailSenderService,
                       EmailConfirmationRepository emailConfirmationRepository) {
        this.userRepository = userRepository;
        this.emailSenderService = emailSenderService;
        this.emailConfirmationRepository = emailConfirmationRepository;
    }


    public Optional<User> registerUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())) {
            return Optional.empty();
        }
        User savedUser = userRepository.save(user);
        emailSenderService.sendEmail(
                savedUser.getEmail(),
                "noreply@ares.com",
                "Confirm Your Email",
                "email-verification",
                "name", user.getEmail(),
                "verificationLink", "http://localhost:8080/verify"
                );
        return Optional.of(savedUser);
    }

    public Optional<User> getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByAccount(Account account){
        return userRepository.findByAccount(account);
    }

    public User saveUser(User user){
        return userRepository.save(user);
    }

    public boolean confirm(String code){

        Optional<EmailConfirmationCode> optionalEcc = emailConfirmationRepository.findById(code);
        if (optionalEcc.isEmpty()){
            return false;
        }

        EmailConfirmationCode ecc = optionalEcc.get();
        String email = ecc.getEmail();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()){
            return false;
        }

        User user = optionalUser.get();
        user.getAccount().enableAccount();
        userRepository.save(user);
        return true;

    }
}
