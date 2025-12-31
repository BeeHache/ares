package net.blackhacker.ares.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.validation.UserDTOValidator;
import net.blackhacker.ares.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserDTOValidator userDTOValidator;

    @MockitoBean
    private MockHttpSession session;

    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void login_shouldReturnOk_whenCredentialsAreValid() throws Exception {
        // Arrange
        UserDTO loginDTO = new UserDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password");

        User userModel = new User();
        userModel.setEmail("test@example.com");

        UserDTO userInSessionDTO = new UserDTO();
        userInSessionDTO.setEmail("test@example.com");

        doNothing().when(userDTOValidator).validateUserForLogin(any(UserDTO.class));
        when(userMapper.toModel(any(UserDTO.class))).thenReturn(userModel);
        when(userService.loginUser(any(User.class))).thenReturn(userModel);
        when(userMapper.toDTO(any(User.class))).thenReturn(userInSessionDTO);

        // Act & Assert
        mockMvc.perform(
                post("/api/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk());

        verify(session).setAttribute(eq("user"), eq(userInSessionDTO));
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        // Arrange
        UserDTO loginDTO = new UserDTO();
        loginDTO.setEmail("wrong@example.com");
        loginDTO.setPassword("wrongpassword");

        User userModel = new User();

        doNothing().when(userDTOValidator).validateUserForLogin(any(UserDTO.class));
        when(userMapper.toModel(any(UserDTO.class))).thenReturn(userModel);
        // Simulate authentication failure
        doThrow(new BadCredentialsException("Invalid credentials")).when(userService).loginUser(any(User.class));

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());

        verify(session, never()).setAttribute(eq("user"), any(UserDTO.class));
    }

    @Test
    void login_shouldReturnBadRequest_whenInputDataIsInvalid() throws Exception {
        // Arrange
        UserDTO loginDTO = new UserDTO(); // Invalid DTO (e.g., missing email/password)

        // Simulate validation failure
        doThrow(new ValidationException("Invalid login input")).when(userDTOValidator).validateUserForLogin(any(UserDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).loginUser(any(User.class));
        verify(session, never()).setAttribute(eq("user"), any(UserDTO.class));
    }
}
