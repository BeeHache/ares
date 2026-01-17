package net.blackhacker.ares.service;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.repository.FeedRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;

@Slf4j
@Service
public class FeedService {

    private final FeedRepository feedRepository;
    private final RssService rssService;

    @Value("${feed.interval_ms}")
    private long feedIntervalMs;

    @Value("${feed.query_limit}")
    private int queryLimit;

    public FeedService(
            FeedRepository feedRepository,
           RssService rssService) {
        this.feedRepository = feedRepository;
        this.rssService = rssService;
    }

    public Feed addFeed(String link) {
        log.info("Adding feed: {}", link);
        Feed feed = feedRepository.findByLink(link);
        if (feed != null) {
            log.debug("Feed already exists: {}", link);
            return feed;
        }
        try {
            feed = rssService.feedFromUrl(link);
            Feed savedFeed = feedRepository.save(feed);
            log.info("Feed added successfully: {}", link);
            return savedFeed;
        } catch (Exception e) {
            log.error("Error adding feed: {}", link, e);
            throw e;
        }
    }

    public Feed getFeedById(Long id){
        return feedRepository.findById(id).orElse(null);
    }

    public Feed saveFeed(Feed feed){
        return feedRepository.save(feed);
    }



    @Async
    void updateFeeds() {
        log.info("Starting feed update cycle");
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusSeconds(feedIntervalMs / 1000);
        log.debug("Five minutes ago:      {}", fiveMinutesAgo);
        LocalDateTime fiveMinutesFromNow = LocalDateTime.now().plusSeconds(feedIntervalMs / 1000);
        log.debug("Five minutes from now: {}", fiveMinutesFromNow);

        boolean notDone = true;

        while(notDone && LocalDateTime.now().isBefore(fiveMinutesFromNow)) {
            Collection<Feed> feeds  = feedRepository.findByLastModifiedAfter(fiveMinutesAgo, queryLimit);
            if (feeds.isEmpty()){
                log.debug("No feeds to update");
                break;
            }

            log.debug("Found {} feeds to update", feeds.size());




            // Logic to update feeds would go here (currently missing in the loop body?)
            // Assuming rssService.update(feed) or similar should be called.
            
            notDone = feeds.size() < queryLimit;
            if (notDone) {
                try {
                    Thread.sleep(1000); // sleep for 1 sec
                } catch (InterruptedException e) {
                    log.warn("Feed update interrupted");
                    break;
                }
            }
        }
        log.info("Feed update cycle completed");
    }
}
