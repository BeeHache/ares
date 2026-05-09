package net.blackhacker.ares.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Slf4j
@Service
public class UserReaperJobScheduler {

    private final JobOperator jobOperator;
    private final Job userReaperJob;
    private final Long feedIntervalMs;

    public UserReaperJobScheduler(JobOperator jobOperator,
                                  @Qualifier("userReaperJob") Job userReaperJob,
                                  @Value("${feed.interval_ms:300000}") Long feedIntervalMs) {
        this.jobOperator = jobOperator;
        this.userReaperJob = userReaperJob;
        this.feedIntervalMs = feedIntervalMs;
    }

    @Scheduled(cron = "0 0/15 * * * ?")
    public void runUserReaperJob() {
        log.info("Starting User Reaper Batch Job");

        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobOperator.start(userReaperJob, params);

        } catch (Exception e) {
            log.error("Error launching user reaper job", e);
        }
    }
}
