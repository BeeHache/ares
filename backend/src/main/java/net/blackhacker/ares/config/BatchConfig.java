package net.blackhacker.ares.config;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import net.blackhacker.ares.service.RssService;
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
import org.springframework.transaction.PlatformTransactionManager;

import java.time.ZonedDateTime;
import java.util.Collections;

@Slf4j
@Configuration
public class BatchConfig {

    private final JobRepository jobRepository;
    private final FeedRepository feedRepository;
    private final RssService rssService;

    public BatchConfig(JobRepository jobRepository, FeedRepository feedRepository, RssService rssService) {
        this.jobRepository = jobRepository;
        this.feedRepository = feedRepository;
        this.rssService = rssService;
    }

    // --- ItemReader: Reads Feeds to be updated ---
    @Bean
    @StepScope // Allows dynamic threshold calculation at runtime
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

    // --- ItemProcessor: Updates a single Feed ---
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
            return null; // Skip writing if update failed
        };
    }

    // --- ItemWriter: Saves updated Feeds ---
    @Bean
    public RepositoryItemWriter<Feed> feedWriter() {
        return new RepositoryItemWriterBuilder<Feed>()
                .repository(feedRepository)
                .methodName("save")
                .build();
    }

    // --- Step ---
    @Bean
    public Step feedUpdateStep(ItemReader<Feed> reader, ItemProcessor<Feed, Feed> processor, ItemWriter<Feed> writer) {
        return new StepBuilder("feedUpdateStep", jobRepository)
                .<Feed, Feed>chunk(5)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant() // Allow skipping individual items on error
                .skip(Exception.class)
                .skipLimit(10)
                .build();
    }

    // --- Job ---
    @Bean
    public Job feedUpdateJob(Step feedUpdateStep) {
        return new JobBuilder("feedUpdateJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(feedUpdateStep)
                .build();
    }
}
