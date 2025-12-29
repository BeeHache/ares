package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = UserMapper.class)
@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @MockitoBean
    private FeedMapper feedMapper;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserMapper userMapper;

    @Test
    void toDTO_shouldMapUserToUserDTO() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword"); // This should not be mapped

        Feed feed1 = new Feed();
        feed1.setTitle("Feed 1");
        Feed feed2 = new Feed();
        feed2.setTitle("Feed 2");

        Set<Feed> feeds = new HashSet<>();
        feeds.add(feed1);
        feeds.add(feed2);
        user.setFeeds(feeds);

        FeedDTO feedDTO1 = new  FeedDTO();
        feedDTO1.setTitle(feed1.getTitle());
        FeedDTO feedDTO2 = new  FeedDTO();
        feedDTO2.setTitle(feed2.getTitle());

        when(feedMapper.toDTO(feed1)).thenReturn(feedDTO1);
        when(feedMapper.toDTO(feed2)).thenReturn(feedDTO2);

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
        assertEquals("encodedPassword", user.getPassword());
        assertTrue(user.getFeeds().isEmpty(), "Feeds should not be mapped from DTO in this direction");
    }
}
