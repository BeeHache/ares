package net.blackhacker.ares.service;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.repository.FeedRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class FeedService {

    private final FeedRepository feedRepository;
    private final RssService rssService;
    private final TransactionTemplate transactionTemplate;

    @Value("${feed.interval_ms}")
    private long feedIntervalMs;

    @Value("${feed.query_limit}")
    private int queryLimit;

    public FeedService(
            FeedRepository feedRepository,
           RssService rssService,
           TransactionTemplate transactionTemplate) {
        this.feedRepository = feedRepository;
        this.rssService = rssService;
        this.transactionTemplate = transactionTemplate;
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

    public Feed getFeedById(UUID id){
        return feedRepository.findById(id).orElse(null);
    }

    public Optional<Feed> getFeedByUrl(URL url){
        return feedRepository.findByUrl(url);
    }

    public Feed saveFeed(Feed feed){
        return feedRepository.save(feed);
    }

    public Collection<Feed> saveFeeds(Collection<Feed> feeds){

        Collection<Feed> existingFeeds = new ArrayList<>();
        Collection<Feed> nonExistingFeeds = new ArrayList<>();
        for (Feed feed : feeds){
            Optional<Feed> ofeed = feedRepository.findByUrl(feed.getUrl());
            ofeed.ifPresentOrElse(existingFeeds::add, () -> nonExistingFeeds.add(feed));
        }

        existingFeeds.addAll(feedRepository.saveAll(nonExistingFeeds));
        return existingFeeds;
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

            int processed = 0;
            for (Feed feed : feeds) {
                try {
                    transactionTemplate.execute(status -> {
                        // Re-fetch the feed to ensure it's attached to the current transaction/session
                        // This allows lazy loading of items to work
                        Feed attachedFeed = feedRepository.findById(feed.getId()).orElseThrow();
                        
                        Feed newFeed = rssService.feedFromUrl(attachedFeed.getUrl().toString());
                        if (newFeed != null) {
                            Collection<FeedItem> feedItems = attachedFeed.getItems();
    
                            //re-parent the newFeed items
                            for (FeedItem item : newFeed.getItems()) {
                                item.setFeed(attachedFeed);
                                feedItems.add(item);
                            }
                        }
    
                        attachedFeed.touch();
                        feedRepository.save(attachedFeed);
                        return null;
                    });
                    processed++;
                } catch (Exception e) {
                    log.error("Error updating feed {}", feed.getId(), e);
                }
            }

            log.debug("Processed {} feeds", processed);

            if (feeds.getTotalElements() < queryLimit){
                break;
            }

            if (ZonedDateTime.now().isAfter(fiveMinutesFromNow)){
                break;
            }

            try {
                log.debug("Sleeping for {} ms", 3000);
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.error("Error sleeping", e);
                break;
            }

        }
        log.info("Feed update cycle completed");
    }

    @JmsListener(destination = "update-feed")
    public void updateFeed(UUID feedId){
        feedRepository.findById(feedId).ifPresent(rssService::updateFeed);
    }
}
