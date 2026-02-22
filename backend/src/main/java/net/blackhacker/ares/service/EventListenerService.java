package net.blackhacker.ares.service;

import net.blackhacker.ares.events.FeedSavedEvent;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EventListenerService {
    final FeedPageService feedPageService;
    final FeedRepository feedRepository;
    final CacheService cacheService;

    public EventListenerService(
            FeedPageService feedPageService,
            CacheService cacheService,
            FeedRepository feedRepository) {
        this.feedPageService = feedPageService;
        this.cacheService = cacheService;
        this.feedRepository = feedRepository;
    }

    @EventListener(FeedSavedEvent.class)
    void feedSaved(FeedSavedEvent event) {
        feedRepository.findById(event.getFeedId()).ifPresent(feed -> {
            //evict each cached page
            feedPageService.getTotalPages(feed.getId()).ifPresent(
                totalPages -> {
                for (int i = 1; i <= totalPages; i++) {
                    String key = feed.getId().toString() + ":" + i;
                    cacheService.evictSingleCacheValue(CacheService.FEED_DTOS_CACHE, key);
                }
                feedPageService.deletePageNumbers(feed.getId());
            });
        });


    }
}
