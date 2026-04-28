package net.blackhacker.ares.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Slf4j
@Service
public class FeedJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job feedUpdateJob;
    private final Long feedIntervalMs;

    public FeedJobScheduler(JobLauncher jobLauncher,
                            Job feedUpdateJob,
                            @Value("${feed.interval_ms:300000}") Long feedIntervalMs) {
        this.jobLauncher = jobLauncher;
        this.feedUpdateJob = feedUpdateJob;
        this.feedIntervalMs = feedIntervalMs;
    }

    @Scheduled(fixedRateString = "${feed.interval_ms:300000}")
    public void runFeedUpdateJob() {
        log.info("Starting Feed Update Batch Job");
        
        try {
            // Calculate threshold based on config
            ZonedDateTime threshold = ZonedDateTime.now().minusNanos(feedIntervalMs * 1_000_000);
            
            JobParameters params = new JobParametersBuilder()
                    .addString("threshold", threshold.toString())
                    .addLong("time", System.currentTimeMillis()) // Ensures uniqueness
                    .toJobParameters();
            
            jobLauncher.run(feedUpdateJob, params);
            
        } catch (Exception e) {
            log.error("Error launching feed update job", e);
        }
    }
}
