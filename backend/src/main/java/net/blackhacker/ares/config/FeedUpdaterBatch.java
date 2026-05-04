package net.blackhacker.ares.config;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import net.blackhacker.ares.service.RssService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.domain.Sort;

import java.time.ZonedDateTime;
import java.util.Collections;

@Slf4j
@Configuration
public class FeedUpdaterBatch {

    @Lazy
    @Autowired
    private JobRepository jobRepository;

    @Lazy
    @Autowired
    private FeedRepository feedRepository;

    @Lazy
    @Autowired
    private RssService rssService;

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
                log.info("Processing feed: {}", feed.getUrl());
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
                .methodName("saveAndFlush")
                .build();
    }

    @Bean
    public Step feedUpdateStep(ItemReader<Feed> feedReader, 
                              ItemProcessor<Feed, Feed> feedProcessor, 
                              ItemWriter<Feed> feedWriter,
                              AsyncTaskExecutor asyncTaskExecutor) {
        return new StepBuilder("feedUpdateStep", jobRepository)
                .<Feed, Feed>chunk(5)
                .reader(feedReader)
                .processor(feedProcessor)
                .writer(feedWriter)
                .taskExecutor(asyncTaskExecutor)
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
}
