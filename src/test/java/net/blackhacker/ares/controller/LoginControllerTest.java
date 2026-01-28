package net.blackhacker.ares.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.RefreshToken;
import net.blackhacker.ares.security.CustomAccessDeniedHandler;
import net.blackhacker.ares.security.JwtAuthenticationEntryPoint;
import net.blackhacker.ares.security.JwtAuthenticationFilter;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.AccountService;
import net.blackhacker.ares.service.JWTService;
import net.blackhacker.ares.service.RefreshTokenService;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.validation.UserDTOValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

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
    private AccountService accountService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @MockitoBean
    private CustomAccessDeniedHandler forbiddenHandler;

    private ObjectMapper objectMapper;
    private UserDTO loginDTO;
    private Account account;
    private User user;
    private RefreshToken refreshToken;

    final private String refreshTokenString = "refresh-token";
    final private String accessTokenString = "access-token";

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        loginDTO = new UserDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("bad password");


        account = new Account();
        account.setUsername(loginDTO.getEmail());
        account.setPassword(loginDTO.getPassword());

        user = new User();
        user.setEmail(loginDTO.getEmail());
        user.setAccount(account);

        refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenString);
        refreshToken.setUsername(loginDTO.getEmail());
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() throws Exception {
        Authentication successfulAuth = new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities());
        String accessTokenString = "access-token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(successfulAuth);
        when(jwtService.generateToken(any(Account.class))).thenReturn(accessTokenString);
        when(accountService.findAccountByUsername(any(String.class))).thenReturn(Optional.of(account));
        when(refreshTokenService.generateToken(account)).thenReturn(new RefreshToken());

        // Act & Assert
        mockMvc.perform(
            post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,  containsString("refreshToken=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/api/login/refresh")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=604800")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("token")))
                .andExpect(content().string(containsString(accessTokenString)));
            }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {

        when(userMapper.toModel(any(UserDTO.class))).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken_whenRefreshTokenIsValid() throws Exception {
        // Arrange

        when(refreshTokenService.findByToken(any(String.class))).thenReturn(Optional.of(refreshToken));
        when(accountService.findAccountByUsername(any(String.class))).thenReturn(Optional.of(account));
        when(jwtService.generateToken(any(Account.class))).thenReturn(accessTokenString);

        // Act & Assert
        mockMvc.perform(get("/api/login/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("refreshToken", refreshTokenString)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(containsString(accessTokenString)));
    }

    @Test
    void logout_shouldClearCookie_whenCalled() throws Exception {
        // Arrange
        when(refreshTokenService.findByToken(any(String.class))).thenReturn(Optional.of(refreshToken));
        doNothing().when(refreshTokenService).deleteRefreshToken(any(String.class));

        // Act & Assert
        mockMvc.perform(post("/api/login/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("refreshToken", refreshTokenString)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }
}
