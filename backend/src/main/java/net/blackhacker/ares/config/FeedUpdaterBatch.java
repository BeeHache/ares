package net.blackhacker.ares.config;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import net.blackhacker.ares.utils.FeedParser;
import org.jspecify.annotations.Nullable;
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
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.*;

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
    private FeedItemRepository feedItemRepository;

    @Lazy
    @Autowired
    private FeedParser feedParser;

    final private RestClient restClient;

    public FeedUpdaterBatch(RestClient restClient) {
        this.restClient = restClient;
    }

    @Bean
    @StepScope
    public RepositoryItemReader<UUID> feedIdReader(@Value("#{jobParameters['threshold']}") String thresholdStr) {
        ZonedDateTime threshold = thresholdStr != null ? ZonedDateTime.parse(thresholdStr) : ZonedDateTime.now().minusMinutes(5);
        
        Map<String, Sort.Direction> sorts = new LinkedHashMap<>();
        sorts.put("lastModified", Sort.Direction.ASC);
        sorts.put("id", Sort.Direction.ASC);

        return new RepositoryItemReaderBuilder<UUID>()
                .name("feedIdReader")
                .repository(feedRepository)
                .methodName("findFeedIdsModifiedBefore")
                .arguments(Collections.singletonList(threshold))
                .pageSize(10)
                .sorts(sorts)
                .build();
    }

    public record FeedIdFilePair(UUID feedId, File file){}


    @Bean
    public ItemProcessor<UUID, FeedIdFilePair> fetchFeedItemProcessor() {
        return new ItemProcessor<>() {

            @Override
            public @Nullable FeedIdFilePair process(UUID feedId) throws Exception {
                Optional<Feed> oFeed = feedRepository.findById(feedId);
                if (oFeed.isEmpty()) {
                    return null;
                }
                Feed feed = oFeed.get();
                if (feed.getUrl() == null) {
                    return null;
                }
                URL url = feed.getUrl();
                final Path path = Files.createTempFile("feed", ".tmp");

                File file = restClient.get()
                        .uri(url.toString())
                        .accept(MediaType.APPLICATION_XML)
                        .exchange((clientRequest, clientResponse) -> {
                            if (!clientResponse.getStatusCode().is2xxSuccessful()) {
                                log.error(clientResponse.toString());
                                return null;
                            }

                            try (InputStream is = clientResponse.getBody()) {
                                Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
                                return path.toFile();
                            } catch (Exception e) {
                                return null;
                            }
                        });
                return new FeedIdFilePair(feedId, file);
            }
        };
    }

    @Bean
    public ItemProcessor<FeedIdFilePair, Collection<FeedItem>> parseFeedItemProcessor() {
        return new ItemProcessor<FeedIdFilePair, Collection<FeedItem>>(){


            @Override
            public @Nullable Collection<FeedItem> process(@NonNull FeedIdFilePair pair) {
                File file = pair.file();
                UUID feedId = pair.feedId();

                if (file == null || !file.exists()) return null;
                try {
                    Optional<Feed> oFeed = feedRepository.findById(feedId);
                    if (oFeed.isEmpty()){
                        return null;
                    }
                    Feed feed = oFeed.get();
                    feedParser.parse(feed, file.toURI().toURL().openStream());
                    return feed.getFeedItems();
                } catch (IOException e) {
                    return null;
                } finally {
                    if (file.exists()) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            log.warn("Failed to delete temporary file: {}", file.getAbsolutePath());
                        }
                    }
                }
            }
        };
    }

    @Bean
    public ItemProcessor<UUID, Collection<FeedItem>> feedProcessor() {
        CompositeItemProcessor<UUID, Collection<FeedItem>> processor = new CompositeItemProcessor<>();
        processor.setDelegates(Arrays.asList(fetchFeedItemProcessor(), parseFeedItemProcessor()));
        return processor;
    }

    @Bean
    public ItemWriter<Collection<FeedItem>> feedItemWriter() {
        return chunk -> {
            for (Collection<FeedItem> items : chunk) {
                for (FeedItem feedItem : items) {
                    try {
                        // Check if the item already exists
                        if (feedItemRepository.findByFeedAndTitle(feedItem.getFeed().getId(), feedItem.getTitle()).isEmpty()) {
                            // Use saveAndFlush so Hibernate executes the INSERT immediately.
                            // This allows us to catch the DataIntegrityViolationException inside this block
                            // instead of at the end of the transaction.
                            feedItemRepository.saveAndFlush(feedItem);
                        }
                    } catch (DataIntegrityViolationException e) {
                        // This handles the race condition where another thread inserted the item
                        // between our check and our save call. We can safely ignore this.
                        log.warn("Ignoring duplicate feed item due to race condition: {} - {}", feedItem.getFeed().getId(), feedItem.getTitle());
                    }
                }
            }
        };
    }

    @Bean
    public Step feedUpdateStep(ItemReader<UUID> feedReader,
                               ItemProcessor<UUID, Collection<FeedItem>> feedProcessor,
                               ItemWriter<Collection<FeedItem>> feedWriter,
                               AsyncTaskExecutor asyncTaskExecutor) {
        return new StepBuilder("feedUpdateStep", jobRepository)
                .<UUID, Collection<FeedItem>>chunk(5)
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
