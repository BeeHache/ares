package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.dto.ImageDTO;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.model.Image;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedItemMapperTest {

    @Mock
    private ImageMapper imageMapper;

    @InjectMocks
    private FeedItemMapper feedItemMapper;


    @Test
    void toDTO_shouldMapFeedItemToFeedItemDTO() {

        final LocalDateTime LDT = LocalDateTime.of(2025, 1, 1, 0, 0);

        // Arrange
        Image image = new Image();
        image.setContentType("image/png");
        image.setData(new byte[]{1, 2, 3});

        FeedItem item = new FeedItem();
        item.setId(1L);
        item.setTitle("Test Item");
        item.setDescription("A test item");
        item.setLink("http://example.com/item");
        item.setImage(image);
        item.setDate(LDT);

        ImageDTO imageDTO = new ImageDTO();
        imageDTO.setContentType("image/png");
        imageDTO.setData(new byte[]{1, 2, 3});

        when(imageMapper.toDTO(any(Image.class))).thenReturn(imageDTO);

        // Act
        FeedItemDTO dto = feedItemMapper.toDTO(item);

        // Assert
        assertNotNull(dto);
        assertEquals(item.getTitle(), dto.getTitle());
        assertEquals(item.getDescription(), dto.getDescription());
        assertEquals(item.getLink(), dto.getLink());
        assertEquals(imageDTO, dto.getImage());
        assertEquals(item.getDate(), dto.getDate());
    }

    @Test
    void toModel_shouldMapFeedItemDTOToFeedItem() {

        final LocalDateTime LDT = LocalDateTime.of(2025, 2, 2, 0, 0);

        // Arrange
        ImageDTO imageDTO = new ImageDTO();
        imageDTO.setContentType("image/jpeg");
        imageDTO.setData(new byte[]{4, 5, 6});

        FeedItemDTO dto = new FeedItemDTO();
        dto.setTitle("Test Item DTO");
        dto.setDescription("A test item DTO");
        dto.setLink("http://example.com/item-dto");
        dto.setImage(imageDTO);
        dto.setDate(LDT);

        Image image = new Image();
        image.setContentType("image/jpeg");
        image.setData(new byte[]{4, 5, 6});

        when(imageMapper.toModel(any(ImageDTO.class))).thenReturn(image);

        // Act
        FeedItem item = feedItemMapper.toModel(dto);

        // Assert
        assertNotNull(item);
        assertEquals(dto.getTitle(), item.getTitle());
        assertEquals(dto.getDescription(), item.getDescription());
        assertEquals(dto.getLink(), item.getLink());
        assertEquals(image, item.getImage());
        assertEquals(LDT, item.getDate());
    }
}
