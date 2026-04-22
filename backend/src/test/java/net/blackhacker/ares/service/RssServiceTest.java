package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import net.blackhacker.ares.utils.FeedParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
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

    @Mock
    private ObjectProvider<FeedParser> feedParserProvider;

    @Mock
    private FeedParser feedParser;

    private RssService rssService;

    private String validRssString;

    @BeforeEach
    void setUp() {
        rssService = new RssService(urlFetchService, feedItemRepository, feedParserProvider);
        
        validRssString = "Valid XML content";
    }

    @Test
    void buildFeedFromUrl_shouldReturnFeed_whenRssIsValid() {
        when(urlFetchService.fetchBytes(eq("http://example.com/rss"), any()))
                .thenReturn(ResponseEntity.ok(validRssString.getBytes(StandardCharsets.UTF_8)));
        when(feedParserProvider.getObject()).thenReturn(feedParser);
        
        // Mock parser behavior to set some data
        doAnswer(invocation -> {
            Feed feed = invocation.getArgument(0);
            feed.setTitle("Parsed Title");
            return null;
        }).when(feedParser).parse(any(Feed.class), any(InputStream.class));

        Feed result = rssService.buildFeedFromUrl("http://example.com/rss");
        
        assertNotNull(result);
        assertEquals("Parsed Title", result.getTitle());
    }

    @Test
    void updateFeed_shouldReturnTrue_whenFeedUpdated() throws URISyntaxException, MalformedURLException {
        Feed feed = new Feed();
        feed.setUrlFromString("http://example.com/rss");

        when(urlFetchService.fetchBytes(eq("http://example.com/rss"), any()))
                .thenReturn(ResponseEntity.ok(validRssString.getBytes(StandardCharsets.UTF_8)));
        when(feedParserProvider.getObject()).thenReturn(feedParser);

        // Mock parser to add one item
        doAnswer(invocation -> {
            Feed tempFeed = invocation.getArgument(0);
            tempFeed.setTitle("New Title");
            FeedItem item = new FeedItem();
            item.setTitle("New Item");
            item.setGuid("guid-123");
            tempFeed.getFeedItems().add(item);
            return null;
        }).when(feedParser).parse(any(Feed.class), any(InputStream.class));

        when(feedItemRepository.findByGuid("guid-123")).thenReturn(Optional.empty());

        boolean result = rssService.updateFeed(feed);

        assertTrue(result);
        assertEquals("New Title", feed.getTitle());
        assertEquals(1, feed.getFeedItems().size());
    }

    @Test
    void updateFeed_shouldSkipDuplicateItems() throws URISyntaxException, MalformedURLException {
        Feed feed = new Feed();
        feed.setUrlFromString("http://example.com/rss");

        when(urlFetchService.fetchBytes(anyString(), any()))
                .thenReturn(ResponseEntity.ok(validRssString.getBytes(StandardCharsets.UTF_8)));
        when(feedParserProvider.getObject()).thenReturn(feedParser);

        doAnswer(invocation -> {
            Feed tempFeed = invocation.getArgument(0);
            FeedItem item = new FeedItem();
            item.setTitle("Existing Item");
            item.setGuid("dup-guid");
            tempFeed.getFeedItems().add(item);
            return null;
        }).when(feedParser).parse(any(Feed.class), any(InputStream.class));

        when(feedItemRepository.findByGuid("dup-guid")).thenReturn(Optional.of(new FeedItem()));

        boolean result = rssService.updateFeed(feed);

        assertTrue(result);
        assertEquals(0, feed.getFeedItems().size()); // Duplicate skipped
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
