package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class FeedMapperTest {

    @Mock
    private FeedItemMapper feedItemMapper;

    @InjectMocks
    private FeedMapper feedMapper;

    @Test
    void toDTO_shouldMapFeedToFeedDTO() {
        // Arrange
        Feed feed = new Feed();
        feed.setId(1L);
        feed.setTitle("Test Feed");
        feed.setDescription("A test feed");
        feed.setLink("http://example.com");
        feed.setImage("http://example.com/image.png");
        feed.setPodcast(false);
        feed.setLastModified(LocalDateTime.now());

        List<FeedItem> items = new ArrayList<>();
        items.add(new FeedItem());
        feed.setItems(items);

        // Act
        FeedDTO dto = feedMapper.toDTO(feed);

        // Assert
        assertNotNull(dto);
        assertEquals(feed.getTitle(), dto.getTitle());
        assertEquals(feed.getDescription(), dto.getDescription());
        assertEquals(feed.getLink(), dto.getLink());
        assertEquals(feed.getImage(), dto.getImage());
        assertEquals(feed.isPodcast(), dto.isPodcast());
        assertNotNull(dto.getLastModified());
        assertNotNull(dto.getItems());
        assertEquals(1, dto.getItems().size());
    }

    @Test
    void toModel_shouldMapFeedDTOToFeed() {
        // Arrange
        FeedDTO dto = new FeedDTO();
        dto.setTitle("Test Feed DTO");
        dto.setDescription("A test feed DTO");
        dto.setLink("http://example.com/dto");
        dto.setImage("http://example.com/dto.png");
        dto.setPodcast(true);
        dto.setLastModified(LocalDateTime.now());

        // Act
        Feed feed = feedMapper.toModel(dto);

        // Assert
        assertNotNull(feed);
        assertEquals(dto.getTitle(), feed.getTitle());
        assertEquals(dto.getDescription(), feed.getDescription());
        assertEquals(dto.getLink(), feed.getLink());
        assertEquals(dto.getImage(), feed.getImage());
        assertEquals(dto.isPodcast(), feed.isPodcast());
        assertNotNull(feed.getLastModified());
    }
}
