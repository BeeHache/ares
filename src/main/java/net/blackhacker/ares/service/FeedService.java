package net.blackhacker.ares.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.EventQueues;
import net.blackhacker.ares.dto.*;
import net.blackhacker.ares.mapper.FeedItemMapper;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.projection.FeedItemProjection;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.*;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
public class FeedService {

    private final FeedRepository feedRepository;
    private final FeedItemRepository feedItemRepository;
    private final RssService rssService;
    private final FeedPageService feedPageService;
    private final JmsTemplate jmsTemplate;
    private final TransactionTemplate transactionTemplate;
    private final CacheService cacheService;
    private final Long feedIntervalMs;
    private final Integer queryLimit;
    private final FeedItemMapper feedItemMapper;

    public FeedService(
            FeedRepository feedRepository,
            FeedItemRepository feedItemRepository,
            URLFetchService urlFetchService,
            RssService rssService,
            FeedPageService feedPageService,
            JmsTemplate jmsTemplate,
            TransactionTemplate transactionTemplate,
            CacheService cacheService,
            FeedItemMapper feedItemMapper,
            @Value("${feed.interval_ms}") Long feedIntervalMs,
            @Value("${feed.query_limit}") Integer queryLimit) {
        this.feedRepository = feedRepository;
        this.feedItemRepository = feedItemRepository;
        this.rssService = rssService;
        this.feedPageService = feedPageService;
        this.jmsTemplate = jmsTemplate;
        this.transactionTemplate = transactionTemplate;
        this.cacheService = cacheService;
        this.feedItemMapper = feedItemMapper;
        this.feedIntervalMs = feedIntervalMs;
        this.queryLimit = queryLimit;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startup() {
        updateFeeds();
    }

    public Feed addFeed(String link) {
        log.info("Adding feed: {}", link);
        try {
            URL url = new URL(link);
            Optional<Feed> oFeed = feedRepository.findByUrl(url);
            if (oFeed.isPresent()) {
                log.debug("Feed already exists: {}", link);
                return oFeed.get();
            }
            
            Feed feed = rssService.buildFeedFromUrl(link);
            Feed savedFeed = feedRepository.save(feed);
            log.info("Feed added successfully: {}", link);
            return savedFeed;
        } catch (MalformedURLException e) {
            log.error("Invalid URL: {}", link, e);
            throw new IllegalArgumentException("Invalid URL: " + link, e);
        } catch (Exception e) {
            log.error("Error adding feed: {}", link, e);
            throw e;
        }
    }


    @NonNull public Optional<Feed> getFeedById(@NonNull UUID id){
        return feedRepository.findById(id);
    }

    public Collection<FeedItemDTO> getFeedItems(@NonNull UUID feedId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 50, Sort.by("date").descending());
        Slice<FeedItem> page = feedItemRepository.findByFeedId(feedId, pageable);
        return page.stream().map(feedItemMapper::toDTO).toList();
    }

    public Feed saveFeed(Feed feed){
        Feed savedFeed =  feedRepository.save(feed);

        //evict each cached page
        Optional<Integer> feedPageCount = feedPageService.getTotalPages(feed.getId());
        if (feedPageCount.isPresent()) {
            for (int i = 1; i <= feedPageCount.get(); i++) {
                String key = savedFeed.getId().toString() + ":" + i;
                cacheService.evictSingleCacheValue(CacheService.FEED_DTOS_CACHE, key);
            }
            feedPageService.deletePageNumbers(feed.getId());
        }

        return savedFeed;
    }

    public Collection<Feed> saveFeeds(Collection<Feed> feeds){

        /*
         * Separates feeds that exist in the DB already and those that are new.
         */

        Collection<Feed> existingFeeds = new ArrayList<>();
        Collection<Feed> newFeeds = new ArrayList<>();
        for (Feed feed : feeds){
            Optional<Feed> ofeed = feedRepository.findByUrl(feed.getUrl());
            ofeed.ifPresentOrElse(existingFeeds::add, () -> newFeeds.add(feed));
        }

        //save the new feeds to the DB
        Stream<Feed> savedFeeds = newFeeds.stream().map(this::saveFeed)
                .map(feed -> {
                    rssService.updateFeed(feed);
                    return feed;
                });

        //combind and return
        return Stream.concat(existingFeeds.stream(), savedFeeds).toList();
    }

    @Async
    public void updateFeeds() {
        log.info("Starting feed update cycle");
        ZonedDateTime fiveMinutesAgo = ZonedDateTime.now().minusSeconds(feedIntervalMs / 1000);
        log.debug("Five minutes ago:      {}", fiveMinutesAgo);
        ZonedDateTime fiveMinutesFromNow = ZonedDateTime.now().plusSeconds(feedIntervalMs / 1000);
        log.debug("Five minutes from now: {}", fiveMinutesFromNow);

        for (int page=0; true; page++) {
            Pageable pageable = PageRequest.of(page, queryLimit, Sort.by("lastModified"));
            Page<UUID> feedIds  = feedRepository.findFeedIdsModifiedBefore(fiveMinutesAgo, pageable);
            if (feedIds.isEmpty()){
                log.debug("No feeds to update");
                break;
            }

            log.debug("Found {} feeds to update", feedIds.getNumberOfElements());

            feedIds.forEach(this::sendUpdateFeedMessage);

            if (feedIds.getTotalElements() < queryLimit){
                break;
            }

            if (ZonedDateTime.now().isAfter(fiveMinutesFromNow)){
                break;
            }
        }
        log.info("Feed update cycle completed");
    }

    private void sendUpdateFeedMessage(UUID feedId){
        jmsTemplate.convertAndSend(EventQueues.FEED_SAVED, feedId);
    }

    @JmsListener(destination = EventQueues.FEED_SAVED)
    public void updateFeed(UUID feedId){
        transactionTemplate.executeWithoutResult(status -> {
            feedRepository.findById(feedId).ifPresent(feed -> {
                if (rssService.updateFeed(feed)) {
                    try {
                        saveFeed(feed);
                    } catch (Throwable e) {
                        status.setRollbackOnly();
                        log.error("Error updating feed: {}: {}", feedId, e.getMessage());
                    }
                }
            });
        });
    }

    public Collection<FeedItemProjection> searchItems(String query) {
        return feedRepository.searchItems(query);
    }
}
