package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.repository.FeedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;

@Service
public class FeedService {

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private ReaderService readerService;

    @Value("${feed.interval_seconds}")
    private long feedIntervalSeconds;

    @Value("${feed.query_limit}")
    private int queryLimit;



    void addFeed(String link) throws IOException {
        readerService.read(link);
    }



    @Async
    void updateFeeds() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusSeconds(feedIntervalSeconds);
        boolean notDone = true;
        Collection<Feed> feeds = feedRepository.findByLastModifiedAfter(fiveMinutesAgo, queryLimit);
        for(;notDone; notDone = feeds.size() < queryLimit) {
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
