package net.blackhacker.ares.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedualerService {

    @Autowired
    private FeedService feedService;


    @Scheduled(fixedRateString = "${feed.interval_seconds}")
    void schedualFeedUpdates(){
        feedService.updateFeeds();
    }
}
