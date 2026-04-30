package net.blackhacker.ares.config;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.CanceledUser;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.repository.jpa.CanceledUserRepository;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import net.blackhacker.ares.service.RssService;
import net.blackhacker.ares.service.UserService;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.time.ZonedDateTime;
import java.util.Collections;

@Slf4j
@Configuration
public class BatchConfig {

    private final JobRepository jobRepository;
    private final FeedRepository feedRepository;
    private final RssService rssService;
    private final CanceledUserRepository canceledUserRepository;
    private final UserService userService;


    public BatchConfig(JobRepository jobRepository,
                       FeedRepository feedRepository, 
                       RssService rssService, 
                       CanceledUserRepository canceledUserRepository, 
                       UserService userService) {
        this.jobRepository = jobRepository;
        this.feedRepository = feedRepository;
        this.rssService = rssService;
        this.canceledUserRepository = canceledUserRepository;
        this.userService = userService;
    }

    // --- Feed Update Job Configuration ---

    @Bean
    @StepScope
    public RepositoryItemReader<Feed> feedReader(@Value("#{jobParameters['threshold']}") String thresholdStr) {
        ZonedDateTime threshold = thresholdStr != null ? ZonedDateTime.parse(thresholdStr) : ZonedDateTime.now().minusMinutes(5);
        
        return new RepositoryItemReaderBuilder<Feed>()
                .name("feedReader")
                .repository(feedRepository)
                .methodName("findFeedsModifiedBefore")
                .arguments(Collections.singletonList(threshold))
                .pageSize(10)
                .sorts(Collections.singletonMap("lastModified", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Feed, Feed> feedProcessor() {
        return feed -> {
            try {
                if (rssService.updateFeed(feed)) {
                    return feed;
                }
            } catch (Exception e) {
                log.error("Failed to process feed {}: {}", feed.getUrl(), e.getMessage());
            }
            return null;
        };
    }

    @Bean
    public RepositoryItemWriter<Feed> feedWriter() {
        return new RepositoryItemWriterBuilder<Feed>()
                .repository(feedRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public Step feedUpdateStep(ItemReader<Feed> feedReader, 
                              ItemProcessor<Feed, Feed> feedProcessor, 
                              ItemWriter<Feed> feedWriter) {
        return new StepBuilder("feedUpdateStep", jobRepository)
                .<Feed, Feed>chunk(5)
                .reader(feedReader)
                .processor(feedProcessor)
                .writer(feedWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
                .build();
    }

    @Bean
    public Job feedUpdateJob(Step feedUpdateStep) {
        return new JobBuilder("feedUpdateJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(feedUpdateStep)
                .build();
    }

    // --- User Reaper Job Configuration ---

    @Bean
    public RepositoryItemReader<CanceledUser> canceledUserReader() {
        return new RepositoryItemReaderBuilder<CanceledUser>()
                .name("canceledUserReader")
                .repository(canceledUserRepository)
                .methodName("findAll")
                .pageSize(10)
                .sorts(Collections.singletonMap("userId", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<CanceledUser, CanceledUser> canceledUserProcessor() {
        return user -> user;
    }

    @Bean
    public ItemWriter<CanceledUser> canceledUserWriter() {
        return chunk -> {
            for (CanceledUser canceledUser : chunk) {
                try {
                    userService.deleteUser(canceledUser.getUserId());
                    canceledUserRepository.delete(canceledUser);
                    log.info("Successfully reaped user with ID: {}", canceledUser.getUserId());
                } catch (Exception e) {
                    log.error("Failed to reap user with ID: {}: {}", canceledUser.getUserId(), e.getMessage());
                    throw e; 
                }
            }
        };
    }

    @Bean
    public Step deleteCanceledUsersStep(ItemReader<CanceledUser> canceledUserReader, 
                                       ItemProcessor<CanceledUser, CanceledUser> canceledUserProcessor, 
                                       ItemWriter<CanceledUser> canceledUserWriter) {
        return new StepBuilder("deleteCanceledUsersStep", jobRepository)
                .<CanceledUser, CanceledUser>chunk(5)
                .reader(canceledUserReader)
                .processor(canceledUserProcessor)
                .writer(canceledUserWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(5)
                .build();
    }

    @Bean
    public Job userReaperJob(Step deleteCanceledUsersStep) {
        return new JobBuilder("userReaperJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(deleteCanceledUsersStep)
                .build();
    }
}
