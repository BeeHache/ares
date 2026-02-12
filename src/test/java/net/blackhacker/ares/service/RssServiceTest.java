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
    void feedDTOFromUrl_shouldReturnFeedDTO_whenRssIsValid() {

        when(urlFetchService
                .fetchBytes(eq("http://example.com/rss"), any()))
                .thenReturn(ResponseEntity.ok(rssContentBytes));

        URL url=null;
        try {
            url = new URI("http://example.com/rss").toURL();
        } catch (Exception e) {fail();}
        FeedDTO result = rssService.feedDTOFromUrl(url);

        assertNotNull(result);
        assertEquals("Test Feed", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals("http://example.com", result.getLink());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertEquals("Item Title", result.getItems().iterator().next().getTitle());
    }

    @Test
    void feedDTOFromUrl_shouldReturnNull_whenRssIsEmpty() {
        ResponseEntity<byte[]> emptyRssOkResponse = ResponseEntity.ok(emptyRssString.getBytes(StandardCharsets.UTF_8));
        
        // RssReader returns empty list if no items are found, but RssService checks if list is empty.
        // However, RssReader.read() returns a stream of Items. If there are no items, the list is empty.
        // But RssService logic: List<Item> rssItems = parseRss(urlString); if (rssItems.isEmpty()) return null;
        // An RSS feed with a channel but no items is valid XML but results in empty items list.


        URL url=null;
        try {
            url = new URI("http://example.com/rss").toURL();
        } catch (Exception e) {fail();}

        when(urlFetchService.fetchBytes(eq("http://example.com/rss"), any()))
                .thenReturn(emptyRssOkResponse);

        FeedDTO result = rssService.feedDTOFromUrl(url);

        assertNull(result);
    }

    @Test
    void feedDTOFromUrl_shouldThrowServiceException_whenReadFails() {
        final URL url;
        try {
            url = new URI("http://example.com/rss").toURL();
            assertThrows(ServiceException.class, () -> rssService.feedDTOFromUrl(url));
        } catch (Exception e) {fail();}

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
