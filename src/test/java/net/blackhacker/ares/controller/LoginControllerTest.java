package net.blackhacker.ares.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.blackhacker.ares.CustomAccessDeniedHandler;
import net.blackhacker.ares.JwtAuthenticationEntryPoint;
import net.blackhacker.ares.JwtAuthenticationFilter;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.RefreshToken;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.JWTService;
import net.blackhacker.ares.service.RefreshTokenService;
import net.blackhacker.ares.validation.UserDTOValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

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


        ResultActions ra = mockMvc.perform(
                post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)));


        /*
                ra.andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.token").value(accessTokenString));

         */
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        // Arrange
        UserDTO loginDTO = new UserDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("wrong-password");


        User userModel = new User();
        userModel.setEmail("test@example.com");

        String accessTokenString = "access-token";

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userModel);
        refreshToken.setToken("refresh-token");

        doNothing().when(userDTOValidator).validateUserForLogin(any(UserDTO.class));
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
        mockMvc.perform(post("/api/login/refresh")
                        .cookie(new Cookie("refreshToken", refreshTokenString)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(accessTokenString));
    }

    @Test
    void logout_shouldClearCookie_whenCalled() throws Exception {
        // Arrange
           String fakeRefreshToken = "some-refresh-token";
        doNothing().when(refreshTokenService).deleteByToken(fakeRefreshToken);

        // Act & Assert
        mockMvc.perform(post("/api/login/logout")
                        .cookie(new Cookie("refreshToken", fakeRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));
    }
}
