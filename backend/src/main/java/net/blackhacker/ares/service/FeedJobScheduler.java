package net.blackhacker.ares.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Slf4j
@Service
public class FeedJobScheduler {

    private final JobOperator jobOperator;
    private final Job feedUpdateJob;
    private final Job userReaperJob;
    private final Long feedIntervalMs;

    public FeedJobScheduler(JobOperator jobOperator,
                            @Qualifier("feedUpdateJob") Job feedUpdateJob,
                            @Qualifier("userReaperJob") Job userReaperJob,
                            @Value("${feed.interval_ms:300000}") Long feedIntervalMs) {
        this.jobOperator = jobOperator;
        this.feedUpdateJob = feedUpdateJob;
        this.userReaperJob = userReaperJob;
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
            jobOperator.start(feedUpdateJob, params);

        } catch (Exception e) {
            log.error("Error launching feed update job", e);
        }
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    public void runUserReaperJob() {
        log.info("Starting User Reaper Batch Job");

        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // Ensures uniqueness
                    .toJobParameters();

            jobOperator.start(userReaperJob, params);

        } catch (Exception e) {
            log.error("Error launching user reaper job", e);
        }
    }
}
