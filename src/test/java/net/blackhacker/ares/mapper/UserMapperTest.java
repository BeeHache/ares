package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = UserMapper.class)
@ExtendWith(MockitoExtension.class)
class UserMapperTest {


    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserMapper userMapper;

    private List<Feed> feeds;
    private Account account;
    private User user;
    private Feed feed1;
    private Feed feed2;
    private FeedDTO feedDTO1;
    private FeedDTO feedDTO2;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setUsername("test@example.com");
        account.setPassword("hashedPassword");

        user = new User();
        user.setEmail("test@example.com");
        user.setAccount(account);

        feed1 = new Feed();
        feed1.setId(UUID.randomUUID());
        feed2 = new Feed();
        feed2.setId(UUID.randomUUID());

        feeds = new ArrayList<>();
        feeds.add(feed1);
        feeds.add(feed2);
        user.setFeeds(feeds);

        feedDTO1 = new FeedDTO();
        feedDTO1.setId(UUID.randomUUID());

        feedDTO2 = new FeedDTO();
        feedDTO2.setId(UUID.randomUUID());

    }

    @Test
    void toDTO_shouldMapUserToUserDTO() {
        // Arrange


        // Act
        UserDTO dto = userMapper.toDTO(user);

        // Assert
        assertNotNull(dto);
        assertEquals("test@example.com", dto.getEmail());
        assertNull(dto.getPassword(), "Password should not be exposed in DTO");
        assertNotNull(dto.getFeeds());
        assertEquals(feeds.size(), dto.getFeeds().size());
    }

    @Test
    void toModel_shouldMapUserDTOToUser() {
        // Arrange
        UserDTO dto = new UserDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        // Act
        User user = userMapper.toModel(dto);

        // Assert
        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("encodedPassword", user.getAccount().getPassword());
        assertTrue(user.getFeeds().isEmpty(), "Feeds should not be mapped from DTO in this direction");
    }
}
