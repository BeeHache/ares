package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.repository.FeedRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FeedService.class)
@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @MockitoBean
    private FeedRepository feedRepository;

    @MockitoBean
    private RssService rssService;

    @MockitoBean
    private TransactionTemplate transactionTemplate;


    @InjectMocks
    private FeedService feedService;

    @Test
    void addFeed_shouldReturnExistingFeed_whenFeedExists() throws URISyntaxException, MalformedURLException {
        // Arrange
        String link = "http://example.com/feed";
        Feed existingFeed = new Feed();
        existingFeed.setLinkFromString(link);

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
        newFeed.setLinkFromString(link);

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
