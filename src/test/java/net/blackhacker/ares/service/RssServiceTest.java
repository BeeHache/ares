package net.blackhacker.ares.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.model.Feed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;

import java.net.URI;
import java.net.URL;
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
    private JmsTemplate jmsTemplate;

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
    void feedFromUrl_shouldReturnFeed_whenRssIsValid() {

        when(urlFetchService
                .fetchBytes(eq("http://example.com/rss"), any()))
                .thenReturn(ResponseEntity.ok(invalidRssString.getBytes(StandardCharsets.UTF_8)));

        Feed result = rssService.feedFromUrl("http://example.com/rss");
        assertNotNull(result);
    }

    @Test
    void feedFromUrl_shouldReturnNull_whenRssIsEmpty() {
        when(urlFetchService
                .fetchBytes(anyString(), any()))
                .thenReturn(ResponseEntity.ok(emptyRssString.getBytes(StandardCharsets.UTF_8)));

        Feed result = rssService.feedFromUrl("http://example.com/rss");

        assertNull(result);
    }
}
