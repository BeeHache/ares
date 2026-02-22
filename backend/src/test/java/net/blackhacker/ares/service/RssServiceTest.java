package net.blackhacker.ares.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RssServiceTest {

    @Mock
    private URLFetchService urlFetchService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FeedItemRepository feedItemRepository;

    @InjectMocks
    private RssService rssService;


    private String rssContentString;
    private byte[] rssContentBytes;
    private String emptyRssString ;
    private String invalidRssString;

    @BeforeEach
    void setUp() {
        rssContentString = """
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

        emptyRssString = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <rss version="2.0">
                <channel>
                </channel>
                </rss>""";

        invalidRssString = """
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

        rssContentBytes = rssContentString.getBytes(StandardCharsets.UTF_8);

    }

    @Test
    void feedFromUrl_shouldReturnBuildFeed_whenRssIsValid() {

        when(urlFetchService
                .fetchBytes(eq("http://example.com/rss"), any()))
                .thenReturn(ResponseEntity.ok(invalidRssString.getBytes(StandardCharsets.UTF_8)));

        Feed result = rssService.buildFeedFromUrl("http://example.com/rss");
        assertNotNull(result);
    }

    @Test
    void buildFeedFromUrl_shouldReturnNull_whenRssIsEmpty() {
        when(urlFetchService
                .fetchBytes(anyString(), any()))
                .thenReturn(ResponseEntity.ok(emptyRssString.getBytes(StandardCharsets.UTF_8)));

        Feed result = rssService.buildFeedFromUrl("http://example.com/rss");

        assertNull(result);
    }
}
