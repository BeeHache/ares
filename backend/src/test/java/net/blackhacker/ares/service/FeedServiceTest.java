package net.blackhacker.ares.service;

import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.events.FeedSavedEvent;
import net.blackhacker.ares.mapper.FeedItemMapper;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class
FeedServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedItemRepository feedItemRepository;

    @Mock
    private URLFetchService  urlFetchService;

    @Mock
    private RssService rssService;

    @Mock
    private CacheService cacheService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private FeedItemMapper feedItemMapper;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private FeedService feedService;

    @BeforeEach
    void setUp() {
        // Reflection to set @Value fields if needed, or use constructor injection in test
        // Since we are using @InjectMocks, we might need to set values manually if they are not mocked
        // But FeedService uses constructor injection for values, so @InjectMocks might not work perfectly for @Value fields
        // Let's instantiate manually to be safe or assume defaults are fine (nulls might cause NPE)
        
        // Re-instantiate with mocks and values
        feedService = new FeedService(
                feedRepository,
                feedItemRepository,
                urlFetchService,
                rssService,
                null, // FeedPageService not used in methods tested
                transactionTemplate,
                cacheService,
                feedItemMapper,
                publisher,
                300000L, // 5 min
                1000
        );
    }

    @Test
    void addFeed_shouldReturnExistingFeed_whenFeedExists() throws URISyntaxException, MalformedURLException {
        // Arrange
        String link = "http://example.com/feed";
        Feed existingFeed = new Feed();
        existingFeed.setUrlFromString(link);

        when(feedRepository.findByUrl(new URI(link).toURL())).thenReturn(Optional.of(existingFeed));

        // Act
        Feed result = feedService.addFeed(link);

        // Assert
        assertNotNull(result);
        assertEquals(existingFeed, result);
        verify(feedRepository, times(1)).findByUrl(new URI(link).toURL());
        verify(rssService, never()).buildFeedFromUrl(anyString());
        verify(feedRepository, never()).save(any(Feed.class));
    }

    @Test
    void addFeed_shouldFetchAndSaveNewFeed_whenFeedDoesNotExist() throws URISyntaxException, MalformedURLException {
        // Arrange
        String link = "http://example.com/new-feed";
        Feed newFeed = new Feed();
        newFeed.setUrlFromString(link);

        when(feedRepository.findByUrl(new URI(link).toURL())).thenReturn(Optional.empty());
        when(rssService.buildFeedFromUrl(link)).thenReturn(newFeed);
        when(feedRepository.save(newFeed)).thenReturn(newFeed);

        // Act
        Feed result = feedService.addFeed(link);

        // Assert
        assertNotNull(result);
        assertEquals(newFeed, result);
        verify(feedRepository, times(1)).findByUrl(new URI(link).toURL());
        verify(rssService, times(1)).buildFeedFromUrl(link);
        verify(feedRepository, times(1)).save(newFeed);
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
        
        // Mock TransactionTemplate behavior
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<Optional<Feed>> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });
        
        when(feedRepository.saveAndFlush(feed)).thenReturn(feed);

        Feed result = feedService.saveFeed(feed);

        assertNotNull(result);
        verify(feedRepository).saveAndFlush(feed);
        verify(publisher).publishEvent(any(FeedSavedEvent.class));
    }

    @Test
    void saveFeeds_shouldSeparateNewAndExisting() throws MalformedURLException {
        Feed newFeed = new Feed();
        newFeed.setUrlFromString("http://new.com");
        Feed existingFeed = new Feed();
        existingFeed.setUrlFromString("http://existing.com");

        when(feedRepository.findByUrl(newFeed.getUrl())).thenReturn(Optional.empty());
        when(feedRepository.findByUrl(existingFeed.getUrl())).thenReturn(Optional.of(existingFeed));
        
        // Mock saveFeed behavior (since it's called internally)
        // Note: Mocking internal calls in the same class is hard with Mockito unless using Spy.
        // But here we are testing the logic of saveFeeds which calls saveFeed.
        // Since we injected mocks into the real service, the internal call to this.saveFeed() 
        // will execute the REAL saveFeed method, which uses the mocked transactionTemplate.
        
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<Optional<Feed>> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });
        when(feedRepository.saveAndFlush(newFeed)).thenReturn(newFeed);

        Collection<Feed> result = feedService.saveFeeds(List.of(newFeed, existingFeed));

        assertEquals(2, result.size());
        verify(rssService).updateFeed(newFeed); // Should update new feed
        verify(rssService, never()).updateFeed(existingFeed); // Should NOT update existing feed (logic in saveFeeds)
    }

    @Test
    void updateFeeds_shouldProcessFeeds() {
        UUID feedId = UUID.randomUUID();
        Page<UUID> page = new PageImpl<>(List.of(feedId));
        
        when(feedRepository.findFeedIdsModifiedBefore(any(ZonedDateTime.class), any(Pageable.class)))
                .thenReturn(page);
        
        // Mock updateFeed(UUID) logic
        Feed feed = new Feed();
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(rssService.updateFeed(feed)).thenReturn(true);
        
        // Mock saveFeed logic
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<Optional<Feed>> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });
        when(feedRepository.saveAndFlush(feed)).thenReturn(feed);

        feedService.updateFeeds();

        verify(rssService).updateFeed(feed);
        verify(feedRepository).saveAndFlush(feed);
    }

    @Test
    void searchItems_shouldCallRepository() {
        String query = "test";
        when(feedRepository.searchItems(query)).thenReturn(List.of());

        feedService.searchItems(query);

        verify(feedRepository).searchItems(query);
    }
}
