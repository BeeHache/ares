package net.blackhacker.ares.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.JWTService;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.validation.UserDTOValidator;
import net.blackhacker.ares.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
public class RegistrationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDTOValidator userDTOValidator;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JWTService jwtService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }


    @Test
    void registerUser_shouldReturnCreated_whenUserDataIsValid() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("ValidPassword123!");

        User user = new User();
        user.setId(1L);
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setFeeds(new java.util.HashSet<>());

        // Mock the validator to do nothing (pass the validation)
        doNothing().when(userDTOValidator).validateUserForRegistration(any(UserDTO.class));
        when(userMapper.toModel(any(UserDTO.class))).thenReturn(user);
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);
        when(userService.registerUser(any(User.class))).thenReturn(user);

        // Act & Assert
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated()) // Expecting 201 OK as the method returns void
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void registerUser_shouldReturnBadRequest_whenUserDataIsInvalid() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO(); // Invalid DTO (e.g., empty email/password)

        // Mock the validator to throw an exception
        doThrow(new ValidationException("Invalid user data"))
                .when(userDTOValidator).validateUserForRegistration(any(UserDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }
}
