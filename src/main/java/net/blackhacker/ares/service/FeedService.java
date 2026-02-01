package net.blackhacker.ares.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.Constants;
import net.blackhacker.ares.dto.FeedTitleDTO;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.repository.FeedRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

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
    private final JmsTemplate jmsTemplate;

    @Value("${feed.interval_ms}")
    private long feedIntervalMs;

    @Value("${feed.query_limit}")
    private int queryLimit;

    public FeedService(
            FeedRepository feedRepository,
           RssService rssService,
            JmsTemplate jmsTemplate) {
        this.feedRepository = feedRepository;
        this.rssService = rssService;
        this.jmsTemplate = jmsTemplate;
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

    public Optional<String> getJsonData(UUID id){
        return feedRepository.getJsonDataById(id);
    }

    public Collection<FeedTitleDTO> getFeedTitles(@NonNull Long userId) {
        return feedRepository.findFeedTitlesByUserId(userId);
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
            feeds.forEach(feed ->{
                sendUpdateFeedMessage(feed.getId());
            });

            log.debug("Processed {} feeds", processed);

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
        jmsTemplate.convertAndSend(Constants.UPDATE_FEED_QUEUE, feedId);
    }

    @JmsListener(destination = Constants.UPDATE_FEED_QUEUE)
    @Transactional // Added Transactional annotation
    public void updateFeed(UUID feedId){
        try {
            feedRepository.findById(feedId).ifPresent(feed -> {
                if (rssService.updateFeed(feed)) {
                    feedRepository.save(feed);
                }
            });
        } catch (Exception e) {
            log.error("Error updating feed: {}: {}", feedId, e.getMessage());
        }
    }
}
