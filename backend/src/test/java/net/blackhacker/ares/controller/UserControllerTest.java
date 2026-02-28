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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({TestConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

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
        feed.setId(UUID.randomUUID());
        feed.setUrlFromString(validUrl);
        feed.setTitle("Test Feed");

        feedDTO = new FeedDTO();
        feedDTO.setId(feed.getId());
        feedDTO.setTitle("Test Feed");
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
    void cancelAccount_shouldDisableAccount() throws Exception {
        when(userService.getUserByAccount(any(Account.class))).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/user/")
                        .with(user(account)))
                .andExpect(status().isOk());

        verify(userService).cancelUser(user);
        verify(accountService).saveAccount(any(Account.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void importOpmlFromFile_shouldReturnOk_whenFileIsValid() throws Exception {
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
    void importOpmlFromUrl_shouldReturnOk_whenUrlIsValid() throws Exception {
        when(userService.getUserByAccount(any(Account.class))).thenReturn(Optional.of(user));
        when(opmlService.importFeed(anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(put("/api/user/import")
                        .param("url", validUrl)
                        .with(user(account)))
                .andExpect(status().isOk());

        verify(urlValidator).validateURL(validUrl);
        verify(opmlService).importFeed(validUrl);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void exportFeeds_shouldReturnXml() throws Exception {
        user.getFeeds().add(feed);
        when(userService.getUserByAccount(any(Account.class))).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/user/export")
                        .with(user(account)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andExpect(content().string(containsString("<opml version=\"2.0\">")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addFeed_shouldReturnOk_whenUserIsLoggedInAndUrlIsValid() throws Exception {

        doNothing().when(urlValidator).validateURL(validUrl);
        when(userService.getUserByAccount(any(Account.class))).thenReturn(Optional.of(user));
        when(feedService.addFeed(anyString())).thenReturn(feed);
        when(feedMapper.toDTO(any(Feed.class))).thenReturn(feedDTO);

        mockMvc.perform(put("/api/user/addfeed")
                        .param("link", validUrl)
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk());

        verify(userService).saveUser(user);
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

    @Test
    @WithMockUser(username = "test@example.com")
    void getFeed_shouldReturnFeedList() throws Exception {
        user.getFeeds().add(feed);
        when(userService.getUserByAccount(any(Account.class))).thenReturn(Optional.of(user));
        when(feedMapper.toDTO(any(Feed.class))).thenReturn(feedDTO);

        mockMvc.perform(get("/api/user/feeds")
                        .with(user(account)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Feed"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void unsubscribeFeed_shouldRemoveFeed() throws Exception {
        user.getFeeds().add(feed);
        when(userService.getUserByAccount(any(Account.class))).thenReturn(Optional.of(user));
        when(feedService.getFeedById(feed.getId())).thenReturn(Optional.of(feed));

        mockMvc.perform(delete("/api/user/feeds/" + feed.getId())
                        .with(user(account)))
                .andExpect(status().isOk());

        verify(userService).saveUser(user);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void unsubscribeFeed_shouldReturnNotFound_whenFeedDoesNotExist() throws Exception {
        when(userService.getUserByAccount(any(Account.class))).thenReturn(Optional.of(user));
        when(feedService.getFeedById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/user/feeds/" + UUID.randomUUID())
                        .with(user(account)))
                .andExpect(status().isNotFound());

        verify(userService, never()).saveUser(any());
    }
}
