package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.model.FeedItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class FeedItemMapperTest {

    @InjectMocks
    private FeedItemMapper feedItemMapper;


    @Test
    void toDTO_shouldMapFeedItemToFeedItemDTO() {

        final LocalDateTime LDT = LocalDateTime.of(2025, 1, 1, 0, 0);

        // Arrange
        FeedItem item = new FeedItem();
        item.setId(1L);
        item.setTitle("Test Item");
        item.setDescription("A test item");
        item.setLink("http://example.com/item");
        item.setImage("http://example.com/item.png");
        item.setDate(LDT);

        // Act
        FeedItemDTO dto = feedItemMapper.toDTO(item);

        // Assert
        assertNotNull(dto);
        assertEquals(item.getTitle(), dto.getTitle());
        assertEquals(item.getDescription(), dto.getDescription());
        assertEquals(item.getLink(), dto.getLink());
        assertEquals(item.getImage(), dto.getImage());
        assertEquals(item.getDate(), dto.getDate());
    }

    @Test
    void toModel_shouldMapFeedItemDTOToFeedItem() {

        final LocalDateTime LDT = LocalDateTime.of(2025, 2, 2, 0, 0);

        // Arrange
        FeedItemDTO dto = new FeedItemDTO();
        dto.setTitle("Test Item DTO");
        dto.setDescription("A test item DTO");
        dto.setLink("http://example.com/item-dto");
        dto.setImage("http://example.com/item-dto.png");
        dto.setDate(LDT);

        // Act
        FeedItem item = feedItemMapper.toModel(dto);

        // Assert
        assertNotNull(item);
        assertEquals(dto.getTitle(), item.getTitle());
        assertEquals(dto.getDescription(), item.getDescription());
        assertEquals(dto.getLink(), item.getLink());
        assertEquals(dto.getImage(), item.getImage());
        assertEquals(LDT, item.getDate());
    }
}
