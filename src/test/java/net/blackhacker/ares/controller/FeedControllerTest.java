package net.blackhacker.ares.controller;

import net.blackhacker.ares.TestConfig;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.security.JwtAuthenticationFilter;
import net.blackhacker.ares.service.FeedService;
import net.blackhacker.ares.service.JWTService;
import net.blackhacker.ares.service.OpmlService;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.validation.MultipartFileValidator;
import net.blackhacker.ares.validation.URLValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedService feedService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private OpmlService opmlService;

    @MockitoBean
    private MultipartFileValidator multipartFileValidator;

    @MockitoBean
    private URLValidator urlValidator;

    @MockitoBean
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private JmsTemplate jmsTemplate;

    private Optional<User> optionalUser;
    private User principal;
    private User user;
    private Feed feed;
    private FeedDTO feedDTO;

    @BeforeEach
    void setup() {
        principal = new User();
        user = new User();
        optionalUser = Optional.of(user);

        feedDTO = new FeedDTO();
        feedDTO.setTitle("Tech Blog");
        feedDTO.setLink("https://tech.blog/rss");

        feed = new Feed();
        feed.setUrlFromString("https://tech.blog/rss");
        feed.setId(UUID.randomUUID());
        feed.setDto(feedDTO);
        user.setFeeds(Set.of(feed));

    }

    @Test
    void getFeed_shouldReturnFeedList_whenUserIsAuthenticated() throws Exception {

        when(userService.getUserByAccount(any(Account.class))).thenReturn(optionalUser);

        Authentication auth = new TestingAuthenticationToken(principal, null, "ROLE_USER");

        // Act & Assert
        mockMvc.perform(get("/api/feed")
                .with(authentication(auth))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Tech Blog"))
                .andExpect(jsonPath("$[0].link").value("https://tech.blog/rss"));
    }

    @Test
    void getFeed_shouldReturnEmptyList_whenUserHasNoFeeds() throws Exception {
        User user = new User();
        user.setFeeds(Collections.emptySet());

        when(userService.getUserByAccount(any(Account.class))).thenReturn(Optional.of(user));

        Authentication auth = new TestingAuthenticationToken(principal, null, "ROLE_USER");

        mockMvc.perform(get("/api/feed")
                .with(authentication(auth))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
