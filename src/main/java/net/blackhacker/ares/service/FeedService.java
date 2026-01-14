package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.FeedRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
public class FeedService {

    private final FeedRepository feedRepository;
    private final RssService rssService;

    @Value("${feed.interval_seconds}")
    private long feedIntervalSeconds;

    @Value("${feed.query_limit}")
    private int queryLimit;

    public FeedService(
            FeedRepository feedRepository,
           RssService rssService) {
        this.feedRepository = feedRepository;
        this.rssService = rssService;
    }

    public Feed addFeed(String link) {
        Feed feed = feedRepository.findByLink(link);
        if (feed != null)
            return feed;
        feed = rssService.feedFromUrl(link);
        return feedRepository.save(feed);
    }

    public Feed getFeedById(Long id){
        return feedRepository.findById(id).orElse(null);
    }

    public Feed saveFeed(Feed feed){
        return feedRepository.save(feed);
    }



    @Async
    void updateFeeds() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusSeconds(feedIntervalSeconds);
        boolean notDone = true;

        for(Collection<Feed> feeds ; notDone; notDone = feeds.size() < queryLimit) {
            feeds = feedRepository.findByLastModifiedAfter(fiveMinutesAgo, queryLimit);

            notDone = feeds.size() < queryLimit;
            if (notDone) {
                try {
                    Thread.sleep(1000); // sleep for 1 sec
                } catch (InterruptedException e) {
                    //breaks out of loop if interrupted
                    break;
                }
            }
        }

    }
}
