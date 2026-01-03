package net.blackhacker.ares.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.blackhacker.ares.CustomAccessDeniedHandler;
import net.blackhacker.ares.JwtAuthenticationEntryPoint;
import net.blackhacker.ares.JwtAuthenticationFilter;
import net.blackhacker.ares.dto.TokenDTO;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.RefreshToken;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.JWTService;
import net.blackhacker.ares.service.RefreshTokenService;
import net.blackhacker.ares.validation.UserDTOValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class LoginControllerTest {

    private WebTestClient webTestClient;

    @MockitoBean
    private UserDTOValidator userDTOValidator;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JWTService jwtService; // Your custom service to sign tokens

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @MockitoBean
    private CustomAccessDeniedHandler forbiddenHandler;

    @MockitoBean
    private Authentication authentication;



    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        webTestClient =
                WebTestClient.bindToController(new LoginController(
                        refreshTokenService, userDTOValidator, authenticationManager, jwtService, userMapper
                )).build();
    }


    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() throws Exception {
        UserDTO loginDTO = new UserDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password");

        User loginModel = new User();
        loginModel.setEmail(loginDTO.getEmail());
        loginModel.setPassword(loginDTO.getPassword());

        String accessTokenString = "access-token";

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(loginModel);
        refreshToken.setToken("refresh-token");

        doNothing().when(userDTOValidator).validateUserForLogin(loginDTO);
        when(userMapper.toModel(loginDTO)).thenReturn(loginModel);
        when(authentication.getPrincipal()).thenReturn(loginModel);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(accessTokenString);
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(refreshToken);

        webTestClient.post().uri("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(loginDTO))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectHeader().value(HttpHeaders.SET_COOKIE, containsString("refreshToken=refresh-token"))
                .expectHeader().value(HttpHeaders.SET_COOKIE, containsString("Path=/api/login/refresh"))
                .expectHeader().value(HttpHeaders.SET_COOKIE, containsString("HttpOnly"))
                .expectHeader().value(HttpHeaders.SET_COOKIE, containsString("Secure"))
                .expectHeader().value(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict"))
                .expectHeader().value(HttpHeaders.SET_COOKIE, containsString("Max-Age=604800"))
                .expectBody(TokenDTO.class).value(dto -> assertThat(dto.getToken()).contains(accessTokenString));
            }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        // Arrange
        UserDTO loginDTO = new UserDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("wrong-password");

        User loginModel = new User();
        loginModel.setEmail(loginDTO.getEmail());
        loginModel.setPassword(loginDTO.getPassword());

        doNothing().when(userDTOValidator).validateUserForLogin(any(UserDTO.class));
        when(userMapper.toModel(loginDTO)).thenReturn(loginModel);
        when(authentication.getPrincipal()).thenReturn(loginModel);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        webTestClient.post().uri("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(objectMapper.writeValueAsString(loginDTO))
                        .exchange()
                        .expectStatus().isUnauthorized();
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken_whenRefreshTokenIsValid() throws Exception {
        // Arrange
        String refreshTokenString = "refresh-token";
        String accessTokenString = "access-token";

        User user = new User();
        user.setEmail("test@example.com");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenString);
        refreshToken.setUser(user);

        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(jwtService.generateToken(user)).thenReturn(accessTokenString);

        // Act & Assert
        webTestClient.post().uri("/api/login/refresh")
                        .cookie("refreshToken", refreshTokenString)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TokenDTO.class).value(dto -> assertThat(dto.getToken()).contains(accessTokenString));
    }

    @Test
    void logout_shouldClearCookie_whenCalled() throws Exception {
        // Arrange
           String fakeRefreshToken = "some-refresh-token";
        doNothing().when(refreshTokenService).deleteByToken(fakeRefreshToken);

        // Act & Assert
        webTestClient.post().uri("/api/login/logout")
                        .cookie("refreshToken", fakeRefreshToken)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().maxAge("refreshToken", Duration.ZERO);
    }
}
