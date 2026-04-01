package net.blackhacker.ares.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.projection.AccountProjection;
import net.blackhacker.ares.repository.jpa.RoleRepository;
import net.blackhacker.ares.repository.jpa.UserRepository;
import net.blackhacker.ares.service.JWTService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JWTService jwtService;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(UserRepository userRepository,
                                     RoleRepository roleRepository,
                                     JWTService jwtService,
                                     @Value("${app.frontend.url:https://localhost}") String frontendUrl) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oAuth2User.getAttribute("email");
        String login = oAuth2User.getAttribute("login");
        
        if (email == null) {
            email = login + "@github.com"; 
        }

        log.info("OAuth2 Login Success: {} ({})", login, email);

        User user = processUserLogin(email, login);
        String token = jwtService.generateToken(user.getAccount());

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login-success")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private User processUserLogin(String email, String username) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Auto-register new user
        log.info("Registering new OAuth2 user: {}", email);
        
        Account account = new Account();
        account.setUsername(email);
        account.setPassword(UUID.randomUUID().toString()); // Random password, they won't use it
        account.setType(AccountProjection.AccountType.USER);
        account.setAccountEnabledAt(ZonedDateTime.now()); // Auto-enable
        
        // Assign default USER role
        account.setRoles(new HashSet<>());
        roleRepository.findByName("USER").ifPresent(role -> account.getRoles().add(role));

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setAccount(account);
        newUser.setFeeds(new HashSet<>());

        // Save directly to repository, bypassing email verification
        return userRepository.save(newUser);
    }
}
