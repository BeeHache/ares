package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.TokenDTO;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.service.JWTService;
import net.blackhacker.ares.service.RefreshTokenService;
import net.blackhacker.ares.validation.UserDTOValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController()
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserDTOValidator userDTOValidator;

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService; // Your custom service to sign tokens

    LoginController(AuthenticationManager authenticationManager, JWTService jwtService){
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping
    ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
        userDTOValidator.validateUserForLogin(userDTO);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDTO.getPassword(), userDTO.getPassword())
        );

        UserDetails userDetails = (UserDetails) Objects.requireNonNull(authentication.getPrincipal());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername()).getToken();
        ResponseCookie cookie = createRefreshCookie(refreshToken);
        TokenDTO accessTokenDTO = TokenDTO.token(accessToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(accessTokenDTO);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name="refreshToken") String refreshToken) {
        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    String newAccessToken = jwtService.generateToken(token.getUser());
                    return ResponseEntity.ok(TokenDTO.token(newAccessToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token missing or invalid"));
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken != null) {
            refreshTokenService.deleteByToken(refreshToken);
        }

        ResponseCookie cleanCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/login/refresh")
                .maxAge(0) // Expires immediately
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body("Logged out successfully");
    }
}
