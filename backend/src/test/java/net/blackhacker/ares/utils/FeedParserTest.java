package net.blackhacker.ares.utils;

import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FeedParserTest {

    private Feed feed;
    private FeedParser feedParser;

    @Mock
    private FeedItemRepository feedItemRepository;


    @BeforeEach
    void setUp() throws Exception {
        feed = new Feed();
        feed.setId(UUID.randomUUID());
        feed.setUrl(new URL("http://example.com/rss"));
        feed.setFeedItems(new HashSet<>());

        feedParser = new FeedParser(feedItemRepository);
        feedParser.setFeed(feed);
    }

    @Test
    void parseRSS_shouldExtractMetadataAndItems() throws Exception {
        String xml = "<rss version=\"2.0\"><channel><title>Test Feed</title><link>http://example.com</link><item><title>Item 1</title><guid>guid1</guid></item></channel></rss>";

        feedParser.parse(feed, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertEquals("Test Feed", feed.getTitle());
        assertEquals(1, feed.getFeedItems().size());
    }

    @Test
    void syncLogic_shouldUpdateExistingItem_whenTitleMatches() throws Exception {
        // Arrange
        UUID existingId = UUID.randomUUID();
        FeedItem existingItem = new FeedItem();
        existingItem.setId(existingId);
        existingItem.setTitle("Known");
        existingItem.setFeed(feed);
        feed.getFeedItems().add(existingItem);

        String xml = "<rss><channel><item><title>Known</title><description>Updated</description></item></channel></rss>";

        // Act
        feedParser.parse(feed, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        // Assert
        assertEquals(1, feed.getFeedItems().size(), "Should merge with existing item");
        FeedItem item = feed.getFeedItems().iterator().next();
        assertEquals(existingId, item.getId(), "ID should be preserved");
        assertEquals("Updated", item.getDescription());
    }

    @Test
    void inMemoryDuplicates_shouldBeMerged() throws Exception {
        String xml = "<rss><channel><item><title>D</title><description>1</description></item><item><title>D</title><description>2</description></item></channel></rss>";

        feedParser.parse(feed, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, feed.getFeedItems().size(), "In-memory duplicates should merge");
        assertEquals("2", feed.getFeedItems().iterator().next().getDescription(), "Should keep last seen data");
    }

    @Test
    void multipleEnclosures_shouldAllBeCaptured() throws Exception {
        String xml = "<rss><channel><item><title>M</title><enclosure url=\"http://a.com\" type=\"a\"/><enclosure url=\"http://b.com\" type=\"b\"/></item></channel></rss>";

        feedParser.parse(feed, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        FeedItem item = feed.getFeedItems().iterator().next();
        assertEquals(2, item.getEnclosures().size());
    }

    @Test
    void invalidItems_shouldBeSkipped() throws Exception {
        String xml = "<rss><channel><item><title></title></item><item><title>V</title></item></channel></rss>";

        feedParser.parse(feed, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, feed.getFeedItems().size());
        assertEquals("V", feed.getFeedItems().iterator().next().getTitle());
    }

    @Test
    void parseAtom_shouldExtractMetadataAndItems() throws Exception {
        String xml = "<feed xmlns=\"http://www.w3.org/2005/Atom\"><title>A</title><link href=\"http://a.com\"/><entry><title>E1</title><link href=\"http://e.com\"/></entry></feed>";

        feedParser.parse(feed, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertEquals("A", feed.getTitle());
        assertEquals(1, feed.getFeedItems().size());
    }
}
