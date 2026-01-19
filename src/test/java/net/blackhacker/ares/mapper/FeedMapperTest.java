package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.dto.ImageDTO;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.model.Image;

import net.blackhacker.ares.utils.URLConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = FeedMapper.class)
@ExtendWith(MockitoExtension.class)
class FeedMapperTest {

    @MockitoBean
    FeedItemMapper feedItemMapper;

    @MockitoBean
    ImageMapper imageMapper;

    @MockitoBean
    URLConverter urlConverter;


    @InjectMocks
    private FeedMapper feedMapper;

    @Test
    void toDTO_shouldMapFeedToFeedDTO() {
        // Arrange
        Image image = new Image();
        image.setContentType("image/png");
        image.setData(new byte[]{1, 2, 3});

        Feed feed = new Feed();
        feed.setId(1L);
        feed.setTitle("Test Feed");
        feed.setDescription("A test feed");
        feed.setLinkString("http://example.com");
        feed.setImage(image);
        feed.setPodcast(false);
        feed.setLastModified(LocalDateTime.now());

        List<FeedItem> items = new ArrayList<>();
        FeedItem feedItem1 = new FeedItem();
        feedItem1.setTitle("Item 1");
        FeedItem feedItem2 = new FeedItem();
        feedItem2.setTitle("Item 2");
        items.add(feedItem1);
        items.add(feedItem2);
        feed.setItems(items);

        FeedItemDTO feedItemDTO1 =  new FeedItemDTO();
        feedItemDTO1.setTitle(feedItem1.getTitle());
        FeedItemDTO feedItemDTO2 =  new FeedItemDTO();
        feedItemDTO2.setTitle(feedItem2.getTitle());

        ImageDTO imageDTO = new ImageDTO();
        imageDTO.setContentType("image/png");
        imageDTO.setData(new byte[]{1, 2, 3});

        when(feedItemMapper.toDTO(feedItem1)).thenReturn(feedItemDTO1);
        when(feedItemMapper.toDTO(feedItem2)).thenReturn(feedItemDTO2);
        when(imageMapper.toDTO(any(Image.class))).thenReturn(imageDTO);

        // Act
        FeedDTO dto = feedMapper.toDTO(feed);

        // Assert
        assertNotNull(dto);
        assertEquals(feed.getTitle(), dto.getTitle());
        assertEquals(feed.getDescription(), dto.getDescription());
        assertEquals(feed.getLink().toString(), dto.getLink());
        assertEquals(imageDTO, dto.getImage());
        assertEquals(feed.isPodcast(), dto.isPodcast());
        assertNotNull(dto.getLastModified());
        assertNotNull(dto.getItems());
        assertEquals(feed.getItems().size(), dto.getItems().size());
    }

    @Test
    void toModel_shouldMapFeedDTOToFeed() {
        // Arrange
        ImageDTO imageDTO = new ImageDTO();
        imageDTO.setContentType("image/jpeg");
        imageDTO.setData(new byte[]{4, 5, 6});

        FeedDTO dto = new FeedDTO();
        dto.setTitle("Test Feed DTO");
        dto.setDescription("A test feed DTO");
        dto.setLink("http://example.com/dto");
        dto.setImage(imageDTO);
        dto.setPodcast(true);
        dto.setLastModified(LocalDateTime.now());

        URL linkURL;
        try {
            linkURL = URI.create(dto.getLink()).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        Image image = new Image();
        image.setContentType("image/jpeg");
        image.setData(new byte[]{4, 5, 6});

        when(imageMapper.toModel(any(ImageDTO.class))).thenReturn(image);
        when(urlConverter.convertToEntityAttribute(any(String.class))).thenReturn(linkURL);

        // Act
        Feed feed = feedMapper.toModel(dto);

        // Assert
        assertNotNull(feed);
        assertEquals(dto.getTitle(), feed.getTitle());
        assertEquals(dto.getDescription(), feed.getDescription());
        assertEquals(dto.getLink(), feed.getLink().toString());
        assertEquals(image, feed.getImage());
        assertEquals(dto.isPodcast(), feed.isPodcast());
        assertNotNull(feed.getLastModified());
    }
}
