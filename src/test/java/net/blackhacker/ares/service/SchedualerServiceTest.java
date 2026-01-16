package net.blackhacker.ares.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SchedualerServiceTest {

    @Mock
    private FeedService feedService;

    @InjectMocks
    private SchedualerService schedualerService;

    @Test
    void schedualFeedUpdates_shouldCallUpdateFeeds() {
        // Act
        schedualerService.schedualFeedUpdates();

        // Assert
        verify(feedService, times(1)).updateFeeds();
    }
}
