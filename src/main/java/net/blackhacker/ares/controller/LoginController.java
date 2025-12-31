package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.TokenDTO;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.JWTService;
import net.blackhacker.ares.service.RefreshTokenService;
import net.blackhacker.ares.validation.UserDTOValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

@RestController()
@RequestMapping("/api/login")
public class LoginController {

    private final RefreshTokenService refreshTokenService;
    private final UserDTOValidator userDTOValidator;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService; // Your custom service to sign tokens
    private final UserMapper userMapper;

    LoginController(RefreshTokenService refreshTokenService, UserDTOValidator userDTOValidator,
                    AuthenticationManager authenticationManager, JWTService jwtService,
                    UserMapper userMapper){
        this.refreshTokenService = refreshTokenService;
        this.userDTOValidator = userDTOValidator;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @PostMapping
    ResponseEntity<TokenDTO> login(@RequestBody UserDTO userDTO) {
        userDTOValidator.validateUserForLogin(userDTO);
        User user = userMapper.toModel(userDTO);


        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user, userDTO.getPassword())
        );

        User userDetails = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername()).getToken();
        ResponseCookie cookie = createRefreshCookie(refreshToken);
        TokenDTO accessTokenDTO = TokenDTO.token(accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok()
                .headers(headers)
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
