package net.blackhacker.ares.service;

import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.events.FeedSavedEvent;
import net.blackhacker.ares.mapper.FeedItemMapper;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedItemRepository feedItemRepository;

    @Mock
    private RssService rssService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private FeedMapper feedMapper;

    @Mock
    private FeedItemMapper feedItemMapper;

    @Mock
    private ApplicationEventPublisher publisher;

    private FeedService feedService;

    @BeforeEach
    void setUp() {
        feedService = new FeedService(
                feedRepository,
                feedItemRepository,
                rssService,
                transactionTemplate,
                feedMapper,
                feedItemMapper,
                publisher
        );
    }

    @Test
    void addFeed_shouldReturnExistingFeed_whenFeedExists() throws URISyntaxException, MalformedURLException {
        String link = "http://example.com/feed";
        Feed existingFeed = new Feed();
        existingFeed.setUrlFromString(link);

        when(feedRepository.findByUrl(new URI(link).toURL())).thenReturn(Optional.of(existingFeed));

        Feed result = feedService.addFeed(link);

        assertNotNull(result);
        assertEquals(existingFeed, result);
    }

    @Test
    void addFeed_shouldFetchAndSaveNewFeed_whenFeedDoesNotExist() throws URISyntaxException, MalformedURLException {
        String link = "http://example.com/new-feed";
        Feed newFeed = new Feed();
        newFeed.setUrlFromString(link);

        when(feedRepository.findByUrl(new URI(link).toURL())).thenReturn(Optional.empty());
        when(rssService.buildFeedFromUrl(link)).thenReturn(newFeed);
        when(feedRepository.save(newFeed)).thenReturn(newFeed);

        Feed result = feedService.addFeed(link);

        assertNotNull(result);
        verify(rssService).buildFeedFromUrl(link);
        verify(feedRepository).save(newFeed);
    }

    @Test
    void updateFeedById_shouldTriggerRssServiceUpdate() {
        UUID id = UUID.randomUUID();
        Feed feed = new Feed();
        feed.setId(id);
        
        when(feedRepository.findById(id)).thenReturn(Optional.of(feed));
        when(rssService.updateFeed(feed)).thenReturn(true);
        
        // Mock saveFeed behavior
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<Optional<Feed>> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });

        feedService.updateFeed(id);

        verify(rssService).updateFeed(feed);
        verify(feedRepository).saveAndFlush(feed);
    }

    @Test
    void getFeedById_shouldReturnFeed_whenFound() {
        UUID id = UUID.randomUUID();
        Feed feed = new Feed();
        when(feedRepository.findById(id)).thenReturn(Optional.of(feed));

        Optional<Feed> result = feedService.getFeedById(id);

        assertTrue(result.isPresent());
        assertEquals(feed, result.get());
    }

    @Test
    void getFeedItems_shouldReturnDTOs() {
        UUID feedId = UUID.randomUUID();
        FeedItem item = new FeedItem();
        FeedItemDTO dto = new FeedItemDTO();
        Slice<FeedItem> slice = new SliceImpl<>(List.of(item));

        when(feedItemRepository.findByFeedId(eq(feedId), any(Pageable.class))).thenReturn(slice);
        when(feedItemMapper.toDTO(item)).thenReturn(dto);

        Collection<FeedItemDTO> result = feedService.getFeedItems(feedId, 0);

        assertEquals(1, result.size());
        verify(feedItemMapper).toDTO(item);
    }

    @Test
    void saveFeed_shouldExecuteInTransactionAndPublishEvent() {
        Feed feed = new Feed();
        feed.setId(UUID.randomUUID());
        
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<Optional<Feed>> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });
        
        when(feedRepository.saveAndFlush(feed)).thenReturn(feed);

        Feed result = feedService.saveFeed(feed);

        assertNotNull(result);
        verify(publisher).publishEvent(any(FeedSavedEvent.class));
    }

    @Test
    void searchItems_shouldCallRepository() {
        String query = "test";
        when(feedRepository.searchItems(query)).thenReturn(List.of());

        feedService.searchItems(query);

        verify(feedRepository).searchItems(query);
    }
}
