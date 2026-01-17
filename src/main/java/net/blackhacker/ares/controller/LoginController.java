package net.blackhacker.ares.controller;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.TokenDTO;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.RefreshToken;
import net.blackhacker.ares.service.AccountService;
import net.blackhacker.ares.service.JWTService;
import net.blackhacker.ares.service.RefreshTokenService;
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

@Slf4j
@RestController()
@RequestMapping("/api/login")
public class LoginController {

    @Value("${security.jwt.refresh_expiration_ms: 86400000}") // default 24 hrs
    private Long refreshTokenDurationMs;

    private final RefreshTokenService refreshTokenService;
    private final AccountService accountService;
    private final UserDTOValidator userDTOValidator;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService; // Your custom service to sign tokens

    public LoginController(RefreshTokenService refreshTokenService, AccountService accountService,
                           UserDTOValidator userDTOValidator,
                           AuthenticationManager authenticationManager, JWTService jwtService){
        this.refreshTokenService = refreshTokenService;
        this.accountService = accountService;
        this.userDTOValidator = userDTOValidator;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping
    ResponseEntity<TokenDTO> login(@RequestBody UserDTO userDTO) {
        log.info("Login attempt for user: {}", userDTO.getEmail());
        userDTOValidator.validateUserForLogin(userDTO);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDTO.getEmail(), userDTO.getPassword())
        );

        Account principal = (Account) authentication.getPrincipal();
        if (principal == null){
            log.warn("Login failed: Principal is null for user: {}", userDTO.getEmail());
            return ResponseEntity.badRequest().build();
        }

        String accessToken = jwtService.generateToken(principal);
        Optional<Account> optionalAccount = accountService.findAccountByUsername(principal.getUsername());

        if (optionalAccount.isEmpty()){
            log.error("Login failed: Account not found in DB for authenticated user: {}", principal.getUsername());
            return ResponseEntity.badRequest().build();
        }

        Account account = optionalAccount.get();
        RefreshToken refreshToken = refreshTokenService.generateToken(account);

        ResponseCookie cookie = createRefreshCookie(refreshToken.getToken());
        TokenDTO accessTokenDTO = TokenDTO.token(accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        
        log.info("Login successful for user: {}", userDTO.getEmail());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .body(accessTokenDTO);
    }

    @GetMapping("/refresh")
    public ResponseEntity<TokenDTO> refreshToken(@CookieValue(name="refreshToken") String refreshToken) {
        log.debug("Refresh token attempt");
        return refreshTokenService.findByToken(refreshToken)
                .map(rt -> {
                    // Token exists in Redis, so it's valid (TTL handles expiration)
                    // We need to find the account associated with this token
                    return accountService.findAccountByUsername(rt.getUsername())
                            .map(account -> {
                                String newAccessToken = jwtService.generateToken(account);
                                log.debug("Token refreshed for user: {}", account.getUsername());
                                return ResponseEntity.ok(TokenDTO.token(newAccessToken));
                            })
                            .orElseThrow(() -> {
                                log.warn("Refresh failed: Account not found for token user: {}", rt.getUsername());
                                return new RuntimeException("Account not found for refresh token");
                            });
                })
                .orElseThrow(() -> {
                    log.warn("Refresh failed: Invalid or expired token");
                    return new RuntimeException("Refresh token missing or invalid");
                });
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken != null) {
            log.debug("Logout request with token");
            refreshTokenService.deleteRefreshToken(refreshToken);
        } else {
            log.debug("Logout request without token");
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
