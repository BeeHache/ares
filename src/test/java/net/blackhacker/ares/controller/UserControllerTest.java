package net.blackhacker.ares.controller;

import net.blackhacker.ares.TestConfig;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.security.CustomAccessDeniedHandler;
import net.blackhacker.ares.security.JwtAuthenticationEntryPoint;
import net.blackhacker.ares.security.JwtAuthenticationFilter;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.*;
import net.blackhacker.ares.validation.MultipartFileValidator;
import net.blackhacker.ares.validation.URLValidator;
import net.blackhacker.ares.validation.UserDTOValidator;
import net.blackhacker.ares.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({TestConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private FeedService feedService;

    @MockitoBean
    private RssService rssService;

    @MockitoBean
    private OpmlService opmlService;


    @MockitoBean
    private JWTService jwtService;

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

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @MockitoBean
    private CustomAccessDeniedHandler forbiddenHandler;

    @MockitoBean
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private JmsTemplate jmsTemplate;

    private Account account;
    private User user;
    private UserDTO userDTO;
    private Feed feed;
    private FeedDTO feedDTO;
    private String validUrl;
    private String invalidUrl;

    @BeforeEach
    void setUp() {

        validUrl = "https://example.com/feed.xml";
        invalidUrl = "not-a-url";

        account = new Account();
        account.setUsername("test@example.com");
        account.setPassword("password");
        account.setRoles(new HashSet<>());

        user = new User();
        user.setEmail("test@example.com");
        user.setFeeds(new HashSet<>());
        user.setAccount(account);

        userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");

        feed = new Feed();
        feed.setUrlFromString(validUrl);

        feedDTO = new FeedDTO();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUser_shouldReturnUserDTO_whenUserIsAuthenticated() throws Exception {
        when(userService.getUserByAccount(any(Account.class))).thenReturn(Optional.of(user));
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        mockMvc.perform(get("/api/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void subscribeUserToFeedsFromUrl_shouldReturnAccepted_whenFileIsValid() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "feeds.opml",
                "text/xml",
                "<opml></opml>".getBytes());
        when(userService.getUserByAccount(any(Account.class))).thenReturn(Optional.of(user));
        when(opmlService.importFile(any())).thenReturn(new ArrayList<>());

        mockMvc.perform(multipart("/api/user/import")
                    .file(multipartFile))
                .andExpect(status().isOk());
        
        verify(multipartFileValidator).validateMultipartFile(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addFeed_shouldReturnOk_whenUserIsLoggedInAndUrlIsValid() throws Exception {

        doNothing().when(urlValidator).validateURL(validUrl);
        when(userService.getUserByAccount(any(Account.class))).thenReturn(Optional.of(user));
        when(feedService.addFeed(anyString())).thenReturn(feed);

        mockMvc.perform(put("/api/user/addfeed")
                        .param("link", validUrl)
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk());

        //verify(userService).saveUser(user);
    }

    @Test
    void addFeed_shouldReturnBadRequest_whenUrlIsInvalid() throws Exception {

        doThrow(new ValidationException("Invalid URL")).when(urlValidator).validateURL(invalidUrl);

        mockMvc.perform(put("/api/user/addfeed")
                        .param("link", invalidUrl)
                        .with(user(account)))
                .andExpect(status().isBadRequest());

        verify(feedService, never()).addFeed(anyString());
    }
}
