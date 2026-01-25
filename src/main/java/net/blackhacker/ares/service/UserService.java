package net.blackhacker.ares.service;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.UserDTO;
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


    final private String frontendUrl;

    public UserService(UserRepository userRepository, EmailSenderService emailSenderService,
                       EmailConfirmationRepository emailConfirmationRepository,
                       @Value("${app.frontend.url:http://localhost:4200}") String frontendUrl) {
        this.userRepository = userRepository;
        this.emailSenderService = emailSenderService;
        this.emailConfirmationRepository = emailConfirmationRepository;
        this.frontendUrl = frontendUrl;
    }

    public User registerUser(User user) {
        log.info("Registering user: {}", user.getEmail());

        Optional<User> oUser  = userRepository.findByEmail(user.getEmail());
        if (oUser.isPresent()) {
            log.warn("User already exists: {}", user.getEmail());
            User foundUser = oUser.get();
            if (foundUser.getAccount().isEnabled()){
                throw new RegistrationException("User already enabled");
            }
            sendVerficationEmail(foundUser);
            return foundUser;
        }

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());
        sendVerficationEmail(savedUser);
        return savedUser;
    }

    public void recoverUser(String email) {
        log.info("Recovering user: {}", email);
        Optional<User> oUser = userRepository.findByEmail(email);
        if (oUser.isEmpty()) {
            log.info("User not found: {}", email);
            return;
        }

        sendRecoveryEmail(oUser.get());
    }

    private void sendVerficationEmail(User user){
        EmailConfirmationCode ecc = new EmailConfirmationCode();
        ecc.setCode(UUID.randomUUID().toString());
        ecc.setEmail(user.getEmail());
        ecc.setTtl(1L); // 1 day
        emailConfirmationRepository.save(ecc);
        log.info("Email confirmation code saved: {}", ecc.getCode());

        String verificationLink = frontendUrl + "/verify/" + ecc.getCode();

        // Note: EmailSenderService.sendEmail signature: (to, from, subject, template, String... args)
        // Args are key, value pairs.
        emailSenderService.sendEmail(
                user.getEmail(),
                "noreply@ares.com",
                "Verify Your Email",
                "email-verification",
                "name", user.getEmail(),
                "verificationLink", verificationLink
        );
        log.info("Email sent to: {}", user.getEmail());
    }

    private void sendRecoveryEmail(User user){
        String code = UUID.randomUUID().toString();
        EmailConfirmationCode ecc = new EmailConfirmationCode();
        ecc.setCode(code);
        ecc.setEmail(user.getEmail());
        ecc.setTtl(1L); // 1 day
        emailConfirmationRepository.save(ecc);
        log.info("Email recovery code saved: {}", code);

        String recoveryLink = frontendUrl + "/verify/" + code;

        // Note: EmailSenderService.sendEmail signature: (to, from, subject, template, String... args)
        // Args are key, value pairs.
        emailSenderService.sendEmail(
                user.getEmail(),
                "noreply@ares.com",
                "Recover Your Email",
                "email-recovery",
                "name", user.getEmail(),
                "recoveryLink", recoveryLink
        );
        log.info("Email sent to: {}", user.getEmail());
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
