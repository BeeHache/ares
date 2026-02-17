package net.blackhacker.ares.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.EventQueues;
import net.blackhacker.ares.dto.*;
import net.blackhacker.ares.mapper.FeedItemMapper;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedImage;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.projection.FeedItemProjection;
import net.blackhacker.ares.projection.FeedSummaryProjection;
import net.blackhacker.ares.projection.FeedTitleProjection;
import net.blackhacker.ares.repository.crud.FeedImageDTORepository;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final FeedImageDTORepository feedImageDTORepository;
    private final URLFetchService urlFetchService;
    private final RssService rssService;
    private final JmsTemplate jmsTemplate;
    private final TransactionTemplate transactionTemplate;
    private final CacheService cacheService;
    private final Long feedIntervalMs;
    private final Integer queryLimit;
    private final FeedMapper feedMapper;
    private final FeedItemMapper feedItemMapper;

    public FeedService(
            FeedRepository feedRepository,
            FeedItemRepository feedItemRepository,
            FeedImageDTORepository feedImageDTORepository,
            URLFetchService urlFetchService,
            RssService rssService,
            JmsTemplate jmsTemplate,
            TransactionTemplate transactionTemplate,
            CacheService cacheService,
            FeedMapper feedMapper,
            FeedItemMapper feedItemMapper,
            @Value("${feed.interval_ms}") Long feedIntervalMs,
            @Value("${feed.query_limit}") Integer queryLimit) {
        this.feedRepository = feedRepository;
        this.feedItemRepository = feedItemRepository;
        this.feedImageDTORepository = feedImageDTORepository;
        this.urlFetchService = urlFetchService;
        this.rssService = rssService;
        this.jmsTemplate = jmsTemplate;
        this.transactionTemplate = transactionTemplate;
        this.cacheService = cacheService;
        this.feedMapper = feedMapper;
        this.feedItemMapper = feedItemMapper;
        this.feedIntervalMs = feedIntervalMs;
        this.queryLimit = queryLimit;
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
            
            Feed feed = rssService.feedFromUrl(link);
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

    public Optional<FeedImage> getFeedImageById(@NonNull UUID id){
        return feedRepository.getFeedImageById(id);
    }

    public Collection<FeedTitleProjection> getFeedTitles(@NonNull Long userId) {
        return feedRepository.findFeedTitlesByUserId(userId).stream()
                .filter(dto -> dto.getTitle() != null)
                .toList();
    }

    public Collection<FeedSummaryProjection> getFeedSummaries(@NonNull Long userId) {
        return feedRepository.findFeedSummariesByUserId(userId).stream()
                .filter(dto -> dto.getTitle() != null)
                .toList();
    }

    public Collection<FeedItemDTO> getFeedItems(@NonNull UUID feedId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 50, Sort.by("date").descending());
        Slice<FeedItem> page = feedItemRepository.findByFeedId(feedId, pageable);
        return page.stream().map(feedItemMapper::toDTO).toList();
    }

    public Optional<Feed> getFeedByUrl(URL url){
        return feedRepository.findByUrl(url);
    }

    public Feed saveFeed(Feed feed){
        Feed savedFeed =  feedRepository.save(feed);
        sendUpdateFeedMessage(feed.getId());
        cacheService.evictSingleCacheValue(CacheService.FEED_DTOS_CACHE, savedFeed.getId());
        cacheService.evictSingleCacheValue(CacheService.FEED_IMAGE_CACHE, savedFeed.getId());
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
        Stream<Feed> savedFeeds = newFeeds.stream().map(this::saveFeed);

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
            Page<Feed> feeds  = feedRepository.findModifiedBefore(fiveMinutesAgo, pageable);
            if (feeds.isEmpty()){
                log.debug("No feeds to update");
                break;
            }

            log.debug("Found {} feeds to update", feeds.getNumberOfElements());

            feeds.forEach(feed ->{
                sendUpdateFeedMessage(feed.getId());
            });

            if (feeds.getTotalElements() < queryLimit){
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
                        updateFeedImage(feed.getFeedImage());
                        saveFeed(feed);
                    } catch (Throwable e) {
                        status.setRollbackOnly();
                        log.error("Error updating feed: {}: {}", feedId, e.getMessage());
                    }
                }
            });
        });
    }

    public void updateFeedImage(FeedImage feedImage) {
        if (feedImage == null){
            return;
        }
        // fetch image content from cache
        feedImageDTORepository.findById(feedImage.getId()).ifPresentOrElse(fidto -> {
            feedImage.setContent(fidto.getContent());
            feedImage.setContentType(MediaType.parseMediaType(fidto.getContentType()));
        }, () -> {
            // fetch image content from internet
            ResponseEntity<byte[]> response = urlFetchService.fetchBytes(feedImage.getImageUrl().toString());
            if (response.getStatusCode().is2xxSuccessful()) {
                feedImage.setContent(response.getBody());
                feedImage.setContentType(response.getHeaders().getContentType());

                // store image content to cache
                FeedImageDTO feedImageDTO = new FeedImageDTO();
                feedImageDTO.setId(feedImage.getId());
                feedImageDTO.setImageUrl(feedImage.getImageUrl().toString());
                feedImageDTO.setContent(feedImage.getContent());
                feedImageDTO.setContentType(feedImage.getContentType().toString());
                feedImageDTORepository.save(feedImageDTO);
            }
        });
    }

    public Collection<FeedItemProjection> searchItems(String query) {
        return feedRepository.searchItems(query);
    }
}
