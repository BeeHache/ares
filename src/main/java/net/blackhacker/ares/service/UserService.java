package net.blackhacker.ares.service;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.EmailConfirmationCode;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.EmailConfirmationRepository;
import net.blackhacker.ares.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailSenderService emailSenderService;
    private final EmailConfirmationRepository emailConfirmationRepository;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public UserService(UserRepository userRepository, EmailSenderService emailSenderService,
                       EmailConfirmationRepository emailConfirmationRepository) {
        this.userRepository = userRepository;
        this.emailSenderService = emailSenderService;
        this.emailConfirmationRepository = emailConfirmationRepository;
    }


    public Optional<User> registerUser(User user) {
        log.info("Registering user: {}", user.getEmail());

        if(userRepository.existsByEmail(user.getEmail())) {
            log.warn("User already exists: {}", user.getEmail());
            return Optional.empty();
        }
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());


        String code = UUID.randomUUID().toString();
        EmailConfirmationCode ecc = new EmailConfirmationCode();
        ecc.setCode(code);
        ecc.setEmail(savedUser.getEmail());
        ecc.setTtl(1L); // 1 day
        emailConfirmationRepository.save(ecc);
        log.info("Email confirmation code saved: {}", code);

        String verificationLink = frontendUrl + "/verify/" + code;

        // Note: EmailSenderService.sendEmail signature: (to, from, subject, template, String... args)
        // Args are key, value pairs.
        emailSenderService.sendEmail(
                savedUser.getEmail(),
                "noreply@ares.com",
                "Verify Your Email",
                "email-verification",
                    "name", user.getEmail(),
                    "verificationLink", verificationLink
        );
        log.info("Email sent to: {}", savedUser.getEmail());
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
        
        // Ideally delete the code after use
        emailConfirmationRepository.delete(ecc);

        return true;

    }
}
