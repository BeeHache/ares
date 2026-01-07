package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.TokenDTO;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.JWTService;
import net.blackhacker.ares.service.RefreshTokenService;
import net.blackhacker.ares.validation.UserDTOValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/login")
public class LoginController {

    private final RefreshTokenService refreshTokenService;
    private final UserDTOValidator userDTOValidator;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService; // Your custom service to sign tokens

    public LoginController(RefreshTokenService refreshTokenService, UserDTOValidator userDTOValidator,
                    AuthenticationManager authenticationManager, JWTService jwtService){
        this.refreshTokenService = refreshTokenService;
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

        User userDetails = (User) authentication.getPrincipal();

        if (userDetails == null){
            return ResponseEntity.badRequest().build();
        }

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername()).getToken();
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
        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    String newAccessToken = jwtService.generateToken(token.getUser());
                    return ResponseEntity.ok(TokenDTO.token(newAccessToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token missing or invalid"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken != null) {
            refreshTokenService.deleteByToken(refreshToken);
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
