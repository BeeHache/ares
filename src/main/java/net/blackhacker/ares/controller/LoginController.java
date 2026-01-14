package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.TokenDTO;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.service.AccountService;
import net.blackhacker.ares.service.JWTService;
import net.blackhacker.ares.validation.UserDTOValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController()
@RequestMapping("/api/login")
public class LoginController {

    @Value("${security.jwt.refresh_expiration_ms: 86400000}") // default 24 hrs
    private Long refreshTokenDurationMs;

    private final AccountService accountService;
    private final UserDTOValidator userDTOValidator;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService; // Your custom service to sign tokens

    public LoginController(AccountService accountService,
                           UserDTOValidator userDTOValidator,
                           AuthenticationManager authenticationManager, JWTService jwtService){
        this.accountService = accountService;
        this.userDTOValidator = userDTOValidator;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping
    ResponseEntity<TokenDTO> login(@RequestBody UserDTO userDTO) {
        userDTOValidator.validateUserForLogin(userDTO);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDTO.getEmail(), userDTO.getPassword())
        );

        Account principal = (Account) authentication.getPrincipal();
        if (principal == null){
            return ResponseEntity.badRequest().build();
        }

        String accessToken = jwtService.generateToken(principal);
        Optional<Account> optionalAccount = accountService.findAccountByUsername(principal.getUsername());

        if (optionalAccount.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        Account account = optionalAccount.get();
        String refreshToken = UUID.randomUUID().toString();
        account.setToken(refreshToken);
        account.setTokenExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenDurationMs)));

        ResponseCookie cookie = createRefreshCookie(refreshToken);
        TokenDTO accessTokenDTO = TokenDTO.token(accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .body(accessTokenDTO);
    }

    @GetMapping("/refresh")
    public ResponseEntity<TokenDTO> refreshToken(@CookieValue(name="refreshToken") String refreshToken) {

        Optional<Account> optionalAccount = accountService.findByToken(refreshToken);
        if (optionalAccount.isEmpty()){
            new RuntimeException("Refresh token missing or invalid");
        }

        Account account = optionalAccount.get();
        if (account.isTokenExpired()){
            new RuntimeException("Refresh token missing or invalid");
        }

        String newAccessToken = jwtService.generateToken(account);
        return ResponseEntity.ok(TokenDTO.token(newAccessToken));

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken != null) {
            Optional<Account> optionalAccount = accountService.findByToken(refreshToken);
            if (optionalAccount.isPresent()) {
                Account account = optionalAccount.get();
                account.setToken(null);
                account.setTokenExpiresAt(LocalDateTime.now());
                accountService.saveAccount(account);
            }
        }

        ResponseCookie cleanCookie = expireRefreshCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body("Logged out successfully");
    }

    private ResponseCookie createRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // Set to true in production (requires HTTPS)
                .path("/api/login/refresh") // Only send to the refresh endpoint
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Strict")
                .build();
    }

    private ResponseCookie expireRefreshCookie() {
        return ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .build();
    }
}
