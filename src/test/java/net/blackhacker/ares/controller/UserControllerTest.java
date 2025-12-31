package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.FeedService;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.service.UtilsService;
import net.blackhacker.ares.validation.MultipartFileValidator;
import net.blackhacker.ares.validation.URLValidator;
import net.blackhacker.ares.validation.UserDTOValidator;
import net.blackhacker.ares.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private UserService userService;

    @MockitoBean
    private FeedService feedService;

    @MockitoBean
    private UtilsService utilsService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private FeedMapper feedMapper;

    @MockitoBean
    private UserDTOValidator userDTOValidator;

    @MockitoBean
    private MultipartFileValidator multipartFileValidator;

    @MockitoBean
    private URLValidator urlValidator;

    private MockHttpSession session = new MockHttpSession();
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        objectMapper = new ObjectMapper();
    }


    @Test
    void registerUser_shouldReturnCreated_whenUserDataIsValid() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("ValidPassword123!");

        User user = new User();

        // Mock the validator to do nothing (pass the validation)
        doNothing().when(userDTOValidator).validateUserForRegistration(any(UserDTO.class));
        when(userMapper.toModel(any(UserDTO.class))).thenReturn(user);
        when(userService.registerUser(any(User.class))).thenReturn(user);

        // Act & Assert
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated()); // Expecting 201 OK as the method returns void
    }

    @Test
    void registerUser_shouldReturnBadRequest_whenUserDataIsInvalid() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO(); // Invalid DTO (e.g., empty email/password)

        // Mock the validator to throw an exception
        doThrow(new ValidationException("Invalid user data"))
                .when(userDTOValidator).validateUserForRegistration(any(UserDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addFeed_shouldReturnOk_whenUserIsLoggedInAndUrlIsValid() throws Exception {
        // Arrange
        String validUrl = "http://example.com/feed.xml";
        UserDTO userInSession = new UserDTO();
        userInSession.setEmail("test@example.com");
        session.setAttribute("user", userInSession);

        Feed newFeed = new Feed();
        newFeed.setTitle("Test Feed");
        newFeed.setLink(validUrl);

        User user = new User();
        user.setEmail("test@example.com");
        user.setFeeds(new HashSet<>());

        FeedDTO feedDTO = new FeedDTO();
        feedDTO.setTitle(newFeed.getTitle());
        feedDTO.setLink(newFeed.getLink());

        // Mock the dependencies

        doNothing().when(urlValidator).validateURL(validUrl);
        when(feedService.addFeed(validUrl)).thenReturn(newFeed);
        when(userService.findByEmail(userInSession.getEmail())).thenReturn(Optional.of(user));
        when(feedMapper.toDTO(any(Feed.class))).thenReturn(feedDTO);

        // Act & Assert
        mockMvc.perform(put("/api/user/addfeed")
                        .session(session)
                        .param("link", validUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(newFeed.getTitle()));

        verify(userService).saveUser(user);
    }

    @Test
    void addFeed_shouldReturnBadRequest_whenUrlIsInvalid() throws Exception {
        // Arrange
        String invalidUrl = "not-a-url";
        UserDTO userInSession = new UserDTO();
        session.setAttribute("user", userInSession);

        doThrow(new ValidationException("Invalid URL")).when(urlValidator).validateURL(invalidUrl);

        // Act & Assert
        mockMvc.perform(put("/api/user/addfeed")
                        .session(session)
                        .param("link", invalidUrl))
                .andExpect(status().isBadRequest());

        verify(feedService, never()).addFeed(anyString());
    }

    @Test
    void addFeed_shouldReturnBadRequest_whenUserNotInSession() throws Exception {
        // Arrange
        String validUrl = "http://example.com/feed.xml";
        // No user in session

        // Act & Assert
        mockMvc.perform(put("/api/user/addfeed")
                        .session(session) // Empty session
                        .param("link", validUrl))
                .andExpect(status().isBadRequest());

        verify(urlValidator, never()).validateURL(anyString());
    }
}
