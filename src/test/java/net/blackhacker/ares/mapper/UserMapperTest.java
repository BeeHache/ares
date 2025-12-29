package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private FeedMapper feedMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserMapper userMapper;

    @Test
    void toDTO_shouldMapUserToUserDTO() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword"); // This should not be mapped

        Set<Feed> feeds = new HashSet<>();
        feeds.add(new Feed());
        user.setFeeds(feeds);

        // Act
        UserDTO dto = userMapper.toDTO(user);

        // Assert
        assertNotNull(dto);
        assertEquals("test@example.com", dto.getEmail());
        assertNull(dto.getPassword(), "Password should not be exposed in DTO");
        assertNotNull(dto.getFeeds());
        assertEquals(1, dto.getFeeds().size());
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
