package net.blackhacker.ares.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Scheduled(fixedRateString = "${feed.interval_ms}")
    void schedualFeedUpdates(){

    }
}
