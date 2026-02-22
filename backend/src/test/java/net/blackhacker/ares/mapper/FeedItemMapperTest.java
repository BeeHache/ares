package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.EnclosureDTO;
import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.model.Enclosure;
import net.blackhacker.ares.model.FeedItem;
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
class FeedItemMapperTest {

    @Mock
    private EnclosureMapper enclosureMapper;

    @InjectMocks
    private FeedItemMapper feedItemMapper;

    @Test
    void toDTO_shouldMapFeedItemToDTO() throws Exception {
        FeedItem feedItem = new FeedItem();
        feedItem.setTitle("Item Title");
        feedItem.setDescription("Item Description");
        feedItem.setLink(new URI("http://example.com/item").toURL());
        ZonedDateTime now = ZonedDateTime.now();
        feedItem.setDate(now);
        feedItem.setEnclosures(Collections.singletonList(new Enclosure()));

        when(enclosureMapper.toDTO(any(Enclosure.class))).thenReturn(new EnclosureDTO());

        FeedItemDTO dto = feedItemMapper.toDTO(feedItem);

        assertNotNull(dto);
        assertEquals("Item Title", dto.getTitle());
        assertEquals("Item Description", dto.getDescription());
        assertEquals("http://example.com/item", dto.getLink());
        assertEquals(now.format(DateTimeFormatter.ISO_INSTANT), dto.getDate());
        assertEquals(1, dto.getEnclosures().size());
    }

    @Test
    void toModel_shouldMapDTOToFeedItem() throws Exception {
        FeedItemDTO dto = new FeedItemDTO();
        dto.setTitle("Item Title");
        dto.setDescription("Item Description");
        dto.setLink("http://example.com/item");
        String dateStr = "2023-10-01T12:00:00Z";
        dto.setDate(dateStr);
        dto.setEnclosures(Collections.singletonList(new EnclosureDTO()));

        when(enclosureMapper.toModel(any(EnclosureDTO.class))).thenReturn(new Enclosure());

        FeedItem feedItem = feedItemMapper.toModel(dto);

        assertNotNull(feedItem);
        assertEquals("Item Title", feedItem.getTitle());
        assertEquals("Item Description", feedItem.getDescription());
        assertEquals("http://example.com/item", feedItem.getLink().toString());
        assertEquals(Instant.parse(dateStr).atZone(ZoneId.of("UTC")), feedItem.getDate());
        assertEquals(1, feedItem.getEnclosures().size());
    }
}
