package net.blackhacker.ares.utils;

import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedParserTest {

    private SAXParser parser;
    private Feed feed;
    private FeedParser handler;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedItemRepository feedItemRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        parser = factory.newSAXParser();
        feed = new Feed();
        feed.setUrl(new URL("http://example.com/rss"));
        
        // Mock transaction template to execute the callback immediately
        lenient().when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });
        
        lenient().doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(mock(TransactionStatus.class));
            return null;
        }).when(transactionTemplate).executeWithoutResult(any(Consumer.class));

        handler = new FeedParser(feedRepository, feedItemRepository, transactionTemplate);
        handler.setFeed(feed);
    }

    @Test
    void parseRSS_shouldExtractMetadataAndItems() throws Exception {
        String xml = """
                <rss version="2.0">
                    <channel>
                        <title>Test Feed</title>
                        <link>http://example.com</link>
                        <description>A test description</description>
                        <item>
                            <title>Item 1</title>
                            <link>http://example.com/1</link>
                            <description>Description 1</description>
                            <pubDate>Mon, 01 Jan 2024 12:00:00 GMT</pubDate>
                            <guid>guid1</guid>
                        </item>
                    </channel>
                </rss>
                """;

        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        parser.parse(is, handler);

        assertEquals("Test Feed", feed.getTitle());
        assertEquals("http://example.com", feed.getLink().toString());
        assertEquals(1, feed.getFeedItems().size());

        FeedItem item = feed.getFeedItems().iterator().next();
        assertEquals("Item 1", item.getTitle());
        assertEquals("guid1", item.getGuid());
        assertNotNull(item.getDate());
        
        verify(feedRepository, atLeastOnce()).saveAndFlush(any(Feed.class));
    }

    @Test
    void multiItem_shouldKeepDataSeparate() throws Exception {
        String xml = """
                <rss version="2.0">
                    <channel>
                        <item>
                            <title>First</title>
                            <guid>g1</guid>
                        </item>
                        <item>
                            <title>Second</title>
                            <guid>g2</guid>
                        </item>
                    </channel>
                </rss>
                """;

        parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), handler);

        List<FeedItem> items = new ArrayList<>(feed.getFeedItems());
        assertEquals(2, items.size());
        
        assertTrue(items.stream().anyMatch(i -> "First".equals(i.getTitle()) && "g1".equals(i.getGuid())));
        assertTrue(items.stream().anyMatch(i -> "Second".equals(i.getTitle()) && "g2".equals(i.getGuid())));
    }

    @Test
    void multipleEnclosures_shouldAllBeCaptured() throws Exception {
        String xml = """
                <rss version="2.0">
                    <channel>
                        <item>
                            <title>Media Item</title>
                            <enclosure url="http://example.com/a.mp3" length="100" type="audio/mpeg" />
                            <enclosure url="http://example.com/b.jpg" length="200" type="image/jpeg" />
                        </item>
                    </channel>
                </rss>
                """;

        parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), handler);

        FeedItem item = feed.getFeedItems().iterator().next();
        assertEquals(2, item.getEnclosures().size());
    }

    @Test
    void invalidItems_shouldBeSkipped() throws Exception {
        String xml = """
                <rss version="2.0">
                    <channel>
                        <item>
                            <title></title> <!-- Invalid -->
                        </item>
                        <item>
                            <title>Valid</title>
                        </item>
                    </channel>
                </rss>
                """;

        parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), handler);

        assertEquals(1, feed.getFeedItems().size());
        assertEquals("Valid", feed.getFeedItems().iterator().next().getTitle());
    }

    @Test
    void contentEncoded_shouldTakePrecedenceOverDescription() throws Exception {
        String xml = """
                <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/">
                    <channel>
                        <item>
                            <title>Complex Content</title>
                            <description>Short summary</description>
                            <content:encoded>Full detailed HTML content</content:encoded>
                        </item>
                    </channel>
                </rss>
                """;

        parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), handler);

        FeedItem item = feed.getFeedItems().iterator().next();
        assertEquals("Full detailed HTML content", item.getDescription());
    }

    @Test
    void parseAtom_shouldExtractMetadataAndItems() throws Exception {
        String xml = """
                <feed xmlns="http://www.w3.org/2005/Atom">
                    <title>Atom Feed</title>
                    <link href="http://example.com" />
                    <entry>
                        <title>Entry 1</title>
                        <link href="http://example.com/entry1" />
                        <summary>Summary 1</summary>
                        <updated>2024-01-01T12:00:00Z</updated>
                    </entry>
                </feed>
                """;

        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        parser.parse(is, handler);

        assertEquals("Atom Feed", feed.getTitle());
        assertEquals("http://example.com", feed.getLink().toString());
        assertEquals(1, feed.getFeedItems().size());

        FeedItem item = feed.getFeedItems().iterator().next();
        assertEquals("Entry 1", item.getTitle());
        assertEquals("http://example.com/entry1", item.getLink().toString());
    }

    @Test
    void characterAccumulation_shouldBeHandledCorrectly() throws Exception {
        String xml = """
                <rss version="2.0">
                    <channel>
                        <title>Part 1 &amp; Part 2</title>
                    </channel>
                </rss>
                """;

        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        parser.parse(is, handler);

        assertEquals("Part 1 & Part 2", feed.getTitle());
    }
}
