package net.blackhacker.ares.service;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.model.Feed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RssServiceTest {

    @MockitoBean
    private URLFetchService urlFetchService;

    private RssService rssService;

    @BeforeEach
    void setUp() {
        rssService = new RssService(urlFetchService);
    }

    @Test
    void feedDTOFromUrl_shouldReturnFeedDTO_whenRssIsValid() {
        String rssContent = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <rss version="2.0">
                <channel>
                  <title>Test Feed</title>
                  <description>Test Description</description>
                  <link>http://example.com</link>
                  <item>
                    <title>Item Title</title>
                    <description>Item Description</description>
                    <link>http://example.com/item</link>
                  </item>
                </channel>
                </rss>""";

        when(urlFetchService.fetchImageBytes("http://example.com/rss")).thenReturn(rssContent.getBytes(StandardCharsets.UTF_8));

        FeedDTO result = rssService.feedDTOFromUrl("http://example.com/rss");

        assertNotNull(result);
        assertEquals("Test Feed", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals("http://example.com", result.getLink());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertEquals("Item Title", result.getItems().getFirst().getTitle());
    }

    @Test
    void feedDTOFromUrl_shouldReturnNull_whenRssIsEmpty() {
        String rssContent = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <rss version="2.0">
                <channel>
                </channel>
                </rss>""";
        
        // RssReader returns empty list if no items are found, but RssService checks if list is empty.
        // However, RssReader.read() returns a stream of Items. If there are no items, the list is empty.
        // But RssService logic: List<Item> rssItems = parseRss(urlString); if (rssItems.isEmpty()) return null;
        // An RSS feed with a channel but no items is valid XML but results in empty items list.
        
        when(urlFetchService.fetchImageBytes("http://example.com/rss")).thenReturn(rssContent.getBytes(StandardCharsets.UTF_8));

        FeedDTO result = rssService.feedDTOFromUrl("http://example.com/rss");

        assertNull(result);
    }

    @Test
    void feedDTOFromUrl_shouldThrowServiceException_whenReadFails() {
        when(urlFetchService.fetchImageBytes(anyString())).thenThrow(new ServiceException("Failed to fetch"));

        assertThrows(ServiceException.class, () -> rssService.feedDTOFromUrl("http://example.com/rss"));
    }

    @Test
    void feedFromUrl_shouldReturnFeed_whenRssIsValid() {
        String rssContent = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <rss version="2.0">
                <channel>
                  <title>Test Feed</title>
                  <description>Test Description</description>
                  <link>http://example.com</link>
                  <item>
                    <title>Item Title</title>
                    <description>Item Description</description>
                    <link>http://example.com/item</link>
                  </item>
                </channel>
                </rss>""";

        when(urlFetchService.fetchImageBytes("http://example.com/rss")).thenReturn(rssContent.getBytes(StandardCharsets.UTF_8));

        Feed result = rssService.feedFromUrl("http://example.com/rss");

        assertNotNull(result);
        assertEquals("Test Feed", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals("http://example.com", result.getLink());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertEquals("Item Title", result.getItems().getFirst().getTitle());
    }

    @Test
    void feedFromUrl_shouldReturnNull_whenRssIsEmpty() {
        String rssContent = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <rss version="2.0">
                <channel>
                </channel>
                </rss>""";

        when(urlFetchService.fetchImageBytes("http://example.com/rss")).thenReturn(rssContent.getBytes(StandardCharsets.UTF_8));

        Feed result = rssService.feedFromUrl("http://example.com/rss");

        assertNull(result);
    }

    @Test
    void feedFromUrl_shouldThrowServiceException_whenReadFails() {
        when(urlFetchService.fetchImageBytes(anyString())).thenThrow(new ServiceException("Failed to fetch"));

        assertThrows(ServiceException.class, () -> rssService.feedFromUrl("http://example.com/rss"));
    }
}
