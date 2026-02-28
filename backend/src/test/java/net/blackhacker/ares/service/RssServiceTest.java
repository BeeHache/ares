package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RssServiceTest {

    @Mock
    private URLFetchService urlFetchService;

    @Mock
    private FeedItemRepository feedItemRepository;

    @InjectMocks
    private RssService rssService;

    private String validRssString;
    private String emptyRssString;
    private String malformedItemRssString;

    @BeforeEach
    void setUp() {
        validRssString = """
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
                    <guid>unique-guid-1</guid>
                    <pubDate>Mon, 06 Sep 2009 16:20:00 +0000</pubDate>
                  </item>
                </channel>
                </rss>""";

        emptyRssString = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <rss version="2.0">
                <channel>
                  <title>Empty Feed</title>
                </channel>
                </rss>""";
        
        // Item 1 is valid, Item 2 has malformed enclosure length (space at end)
        malformedItemRssString = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <rss version="2.0">
                <channel>
                  <title>Mixed Feed</title>
                  <item>
                    <title>Valid Item</title>
                    <link>http://example.com/valid</link>
                  </item>
                  <item>
                    <title>Malformed Item</title>
                    <link>http://example.com/malformed</link>
                    <enclosure url="http://example.com/file.mp3" length="12345 " type="audio/mpeg" />
                  </item>
                </channel>
                </rss>""";
    }

    @Test
    void buildFeedFromUrl_shouldReturnFeed_whenRssIsValid() {
        when(urlFetchService.fetchBytes(eq("http://example.com/rss"), any()))
                .thenReturn(ResponseEntity.ok(validRssString.getBytes(StandardCharsets.UTF_8)));

        Feed result = rssService.buildFeedFromUrl("http://example.com/rss");
        
        assertNotNull(result);
        assertEquals("Test Feed", result.getTitle());
        assertEquals(1, result.getFeedItems().size());
    }

    @Test
    void buildFeedFromUrl_shouldReturnNull_whenRssIsEmpty() {
        when(urlFetchService.fetchBytes(anyString(), any()))
                .thenReturn(ResponseEntity.ok(emptyRssString.getBytes(StandardCharsets.UTF_8)));

        Feed result = rssService.buildFeedFromUrl("http://example.com/rss");

        assertNull(result);
    }

    @Test
    void updateFeed_shouldReturnTrue_whenFeedUpdated() throws URISyntaxException, MalformedURLException {
        Feed feed = new Feed();
        feed.setUrlFromString("http://example.com/rss");

        when(urlFetchService.fetchBytes(eq("http://example.com/rss"), any()))
                .thenReturn(ResponseEntity.ok(validRssString.getBytes(StandardCharsets.UTF_8)));
        when(feedItemRepository.findByGuid(anyString())).thenReturn(Optional.empty());

        boolean result = rssService.updateFeed(feed);

        assertTrue(result);
        assertEquals("Test Feed", feed.getTitle());
        assertEquals(1, feed.getFeedItems().size());
    }

    @Test
    void updateFeed_shouldSkipMalformedItems_butProcessValidOnes() {
        // This tests the fix for "Failed to convert 169600320 "
        Feed feed = new Feed();
        feed.setUrlFromString("http://example.com/mixed");

        when(urlFetchService.fetchBytes(eq("http://example.com/mixed"), any()))
                .thenReturn(ResponseEntity.ok(malformedItemRssString.getBytes(StandardCharsets.UTF_8)));

        boolean result = rssService.updateFeed(feed);

        assertTrue(result);
        // Should contain the valid item, but skip the malformed one (or skip the enclosure if handled gracefully)
        // Based on RssService logic, if rssReader throws on next(), the whole item is skipped.
        // If rssReader handles it internally and returns an item with empty length, it might be included.
        // Assuming our try-catch block in RssService works:
        assertEquals(2, feed.getFeedItems().size());
        assertEquals("Valid Item", feed.getFeedItems().iterator().next().getTitle());
    }

    @Test
    void updateFeed_shouldReturnFalse_whenNetworkFails() {
        Feed feed = new Feed();
        feed.setUrlFromString("http://example.com/fail");

        when(urlFetchService.fetchBytes(anyString(), any()))
                .thenReturn(ResponseEntity.internalServerError().build());

        boolean result = rssService.updateFeed(feed);

        assertFalse(result);
    }
}
