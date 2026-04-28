package net.blackhacker.ares.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.*;
import net.blackhacker.ares.events.FeedSavedEvent;
import net.blackhacker.ares.mapper.FeedItemMapper;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.projection.FeedItemProjection;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
public class FeedService {

    private final FeedRepository feedRepository;
    private final FeedItemRepository feedItemRepository;
    private final RssService rssService;

    private final TransactionTemplate transactionTemplate;

    private final FeedMapper feedMapper;
    private final FeedItemMapper feedItemMapper;
    private final ApplicationEventPublisher publisher;

    public FeedService(
            FeedRepository feedRepository,
            FeedItemRepository feedItemRepository,
            RssService rssService,
            TransactionTemplate transactionTemplate,
            FeedMapper feedMapper,
            FeedItemMapper feedItemMapper,
            ApplicationEventPublisher publisher) {
        this.feedRepository = feedRepository;
        this.feedMapper = feedMapper;
        this.feedItemRepository = feedItemRepository;
        this.rssService = rssService;
        this.transactionTemplate = transactionTemplate;
        this.feedItemMapper = feedItemMapper;
        this.publisher = publisher;
    }

    public Page<Feed> findAllFeeds(Pageable pageable) {
        return feedRepository.findAll(pageable);
    }

    public Long feedCount() {
        return feedRepository.count();
    }

    public  Long feedItemsCount() {
        return feedItemRepository.count();
    }

    public Long feedSubscriberCount(UUID feedId) {
        return feedRepository.findSubscriptionCountByFeedId(feedId);
    }

    public Boolean feedExists(UUID feedId) {
        return feedRepository.existsById(feedId);
    }

    public void deleteFeed(UUID feedId){
        feedRepository.deleteById(feedId);
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
        Optional<Feed> savedFeed = transactionTemplate.execute(status -> {
            try {
                return Optional.of(feedRepository.saveAndFlush(feed));
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("Error updating feed: {}: {}", feed.getId(), e.getMessage());
                return Optional.empty();
            }
        });

        if (savedFeed.isEmpty()) {
            return null;
        }

        publisher.publishEvent(new FeedSavedEvent(savedFeed.get().getId()));
        return savedFeed.get();
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

        List<Feed> savedFeeds = newFeeds.stream()
                .map(this::saveFeed) //save the new feeds to the DB
                .peek(rssService::updateFeed) // update those feeds from the internet
                .toList();

        //combine and return
        return Stream.concat(existingFeeds.stream(), savedFeeds.stream()).toList();
    }

    @Async
    public void updateFeed(UUID feedId){
            feedRepository.findById(feedId).ifPresent(feed -> {
                log.info("Updating feed: {}", feed.getUrl());
                if (rssService.updateFeed(feed)) {
                    saveFeed(feed);
                }
            });

    }

    public Collection<FeedItemProjection> searchItems(String query) {
        return feedRepository.searchItems(query);
    }
}
