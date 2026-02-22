package net.blackhacker.ares.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {

    final private FeedService feedService;

    SchedulerService(FeedService feedService){
        this.feedService = feedService;
    }
    @Scheduled(fixedRateString = "${feed.interval_ms}")
    void schedualFeedUpdates(){
        feedService.updateFeeds();
    }
}
