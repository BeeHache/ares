package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.repository.crud.FeedImageDTORepository;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedImageDTORepository  feedImageDTORepository;

    @Mock
    private URLFetchService  urlFetchService;

    @Mock
    private RssService rssService;

    @Mock
    private CacheService cacheService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private JmsTemplate jmsTemplate;


    @InjectMocks
    private FeedService feedService;

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
        verify(rssService, never()).feedFromUrl(anyString());
        verify(feedRepository, never()).save(any(Feed.class));
    }

    @Test
    void addFeed_shouldFetchAndSaveNewFeed_whenFeedDoesNotExist() throws URISyntaxException, MalformedURLException {
        // Arrange
        String link = "http://example.com/new-feed";
        Feed newFeed = new Feed();
        newFeed.setUrlFromString(link);

        when(feedRepository.findByUrl(new URI(link).toURL())).thenReturn(Optional.empty());
        when(rssService.feedFromUrl(link)).thenReturn(newFeed);
        when(feedRepository.save(newFeed)).thenReturn(newFeed);

        // Act
        Feed result = feedService.addFeed(link);

        // Assert
        assertNotNull(result);
        assertEquals(newFeed, result);
        verify(feedRepository, times(1)).findByUrl(new URI(link).toURL());
        verify(rssService, times(1)).feedFromUrl(link);
        verify(feedRepository, times(1)).save(newFeed);
    }
}
