package net.blackhacker.ares.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FeedItemTest {

    @Test
    void equals_shouldReturnTrue_whenSameObject() {
        FeedItem item = new FeedItem();
        assertEquals(item, item);
    }

    @Test
    void equals_shouldReturnFalse_whenNull() {
        FeedItem item = new FeedItem();
        assertNotEquals(null, item);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentClass() {
        FeedItem item = new FeedItem();
        assertNotEquals("string", item);
    }

    @Test
    void equals_shouldReturnTrue_whenGuidsAreEqual() {
        FeedItem item1 = new FeedItem();
        item1.setGuid("guid123");
        
        FeedItem item2 = new FeedItem();
        item2.setGuid("guid123");

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void equals_shouldReturnFalse_whenGuidsAreDifferent() {
        FeedItem item1 = new FeedItem();
        item1.setGuid("guid1");
        
        FeedItem item2 = new FeedItem();
        item2.setGuid("guid2");

        assertNotEquals(item1, item2);
    }

    @Test
    void equals_shouldReturnFalse_whenOnlyOneHasGuid() {
        FeedItem item1 = new FeedItem();
        item1.setGuid("guid1");
        
        FeedItem item2 = new FeedItem();
        item2.setTitle("Same Title");

        assertNotEquals(item1, item2);
    }

    @Test
    void equals_shouldReturnTrue_whenTitleAndFeedIdAreEqual_andGuidIsNull() {
        UUID feedId = UUID.randomUUID();
        Feed feed1 = new Feed();
        feed1.setId(feedId);

        Feed feed2 = new Feed();
        feed2.setId(feedId);

        FeedItem item1 = new FeedItem();
        item1.setTitle("Same Title");
        item1.setFeed(feed1);
        
        FeedItem item2 = new FeedItem();
        item2.setTitle("Same Title");
        item2.setFeed(feed2);

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void equals_shouldBeSafe_whenFeedsAreNull() {
        FeedItem item1 = new FeedItem();
        item1.setTitle("Same Title");
        
        FeedItem item2 = new FeedItem();
        item2.setTitle("Same Title");

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void equals_shouldReturnFalse_whenTitlesAreDifferent_andGuidIsNull() {
        FeedItem item1 = new FeedItem();
        item1.setTitle("Title 1");
        
        FeedItem item2 = new FeedItem();
        item2.setTitle("Title 2");

        assertNotEquals(item1, item2);
    }

    @Test
    void equals_shouldReturnFalse_whenFeedIdsAreDifferent_andGuidIsNull() {
        Feed feed1 = new Feed();
        feed1.setId(UUID.randomUUID());

        Feed feed2 = new Feed();
        feed2.setId(UUID.randomUUID());

        FeedItem item1 = new FeedItem();
        item1.setTitle("Same Title");
        item1.setFeed(feed1);
        
        FeedItem item2 = new FeedItem();
        item2.setTitle("Same Title");
        item2.setFeed(feed2);

        assertNotEquals(item1, item2);
    }
}
