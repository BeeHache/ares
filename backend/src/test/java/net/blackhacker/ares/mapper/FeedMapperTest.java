package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedMapperTest {

    @InjectMocks
    private FeedMapper feedMapper;

    @Test
    void toDTO_shouldMapFeedToDTO() throws Exception {
        Feed feed = new Feed();
        feed.setTitle("Feed Title");
        feed.setDescription("Feed Description");
        feed.setLink(new URI("http://example.com/feed").toURL());
        feed.setPodcast(true);
        ZonedDateTime now = ZonedDateTime.now();
        FeedItem fi = new  FeedItem();
        fi.setDate(now);
        feed.setFeedItems(Collections.singleton(fi));

        FeedItemDTO feedItemDTO = new FeedItemDTO();
        feedItemDTO.setDate(now.format(DateTimeFormatter.ISO_INSTANT));

        FeedDTO dto = feedMapper.toDTO(feed);

        assertNotNull(dto);
        assertEquals("Feed Title", dto.getTitle());
        assertEquals("Feed Description", dto.getDescription());
        assertEquals("http://example.com/feed", dto.getLink());
        assertTrue(dto.getIsPodcast());
    }

    @Test
    void toModel_shouldMapDTOToFeed() throws Exception {
        FeedDTO dto = new FeedDTO();
        dto.setTitle("Feed Title");
        dto.setDescription("Feed Description");
        dto.setLink("http://example.com/feed");
        dto.setIsPodcast(true);
        ZonedDateTime now = ZonedDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ISO_INSTANT);

        FeedItemDTO fidto = new FeedItemDTO();
        fidto.setDate(dateStr);
        dto.getItems().add(fidto);

        FeedItem fi =  new FeedItem();
        fi.setDate(now);

        Feed feed = feedMapper.toModel(dto);

        assertNotNull(feed);
        assertEquals("Feed Title", feed.getTitle());
        assertEquals("Feed Description", feed.getDescription());
        assertEquals("http://example.com/feed", feed.getLink().toString());
        assertTrue(feed.isPodcast());
    }
}
