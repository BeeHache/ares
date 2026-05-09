package net.blackhacker.ares.config;

import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import net.blackhacker.ares.utils.FeedParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedUpdaterBatchTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedItemRepository feedItemRepository;

    @Mock
    private FeedParser feedParser;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private FeedUpdaterBatch feedUpdaterBatch;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(feedUpdaterBatch, "feedItemRepository", feedItemRepository);
        ReflectionTestUtils.setField(feedUpdaterBatch, "feedRepository", feedRepository);
        ReflectionTestUtils.setField(feedUpdaterBatch, "feedParser", feedParser);
        ReflectionTestUtils.setField(feedUpdaterBatch, "jobRepository", jobRepository);
    }

    @Test
    void feedItemWriter_shouldSaveNewItemsAndIgnoreExisting() throws Exception {
        // Arrange
        ItemWriter<Collection<FeedItem>> writer = feedUpdaterBatch.feedItemWriter();

        Feed feed = new Feed();
        feed.setId(UUID.randomUUID());

        FeedItem item1 = new FeedItem();
        item1.setTitle("Title 1");
        item1.setFeed(feed);

        FeedItem item2 = new FeedItem();
        item2.setTitle("Title 2");
        item2.setFeed(feed);

        Collection<FeedItem> items = List.of(item1, item2);
        Chunk<Collection<FeedItem>> chunk = new Chunk<>(List.of(items));

        when(feedItemRepository.findByFeedAndTitle(feed.getId(), "Title 1")).thenReturn(Optional.empty());
        when(feedItemRepository.findByFeedAndTitle(feed.getId(), "Title 2")).thenReturn(Optional.of(item2));

        // Act
        writer.write(chunk);

        // Assert
        verify(feedItemRepository).saveAndFlush(item1);
        verify(feedItemRepository, never()).saveAndFlush(item2);
    }

    @Test
    void feedItemWriter_shouldHandleDataIntegrityViolationExceptionGracefully() throws Exception {
        // Arrange
        ItemWriter<Collection<FeedItem>> writer = feedUpdaterBatch.feedItemWriter();

        Feed feed = new Feed();
        feed.setId(UUID.randomUUID());

        FeedItem item1 = new FeedItem();
        item1.setTitle("Title 1");
        item1.setFeed(feed);

        Collection<FeedItem> items = List.of(item1);
        Chunk<Collection<FeedItem>> chunk = new Chunk<>(List.of(items));

        when(feedItemRepository.findByFeedAndTitle(feed.getId(), "Title 1")).thenReturn(Optional.empty());
        when(feedItemRepository.saveAndFlush(item1)).thenThrow(new DataIntegrityViolationException("Duplicate key"));

        // Act & Assert (Should not throw exception)
        assertDoesNotThrow(() -> writer.write(chunk));

        verify(feedItemRepository).saveAndFlush(item1);
    }

    @Test
    void parseFeedItemProcessor_shouldReturnNullIfFileDoesNotExist() throws Exception {
        // Arrange
        ItemProcessor<FeedUpdaterBatch.FeedIdFilePair, Collection<FeedItem>> processor = feedUpdaterBatch.parseFeedItemProcessor();
        
        FeedUpdaterBatch.FeedIdFilePair pair = new FeedUpdaterBatch.FeedIdFilePair(UUID.randomUUID(), new File("non-existent-file.xml"));

        // Act
        Collection<FeedItem> result = processor.process(pair);

        // Assert
        assertNull(result);
    }
}
