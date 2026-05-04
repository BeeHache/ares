package net.blackhacker.ares.config;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.CanceledUser;
import net.blackhacker.ares.repository.jpa.CanceledUserRepository;
import net.blackhacker.ares.service.UserService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.domain.Sort;

import java.util.Collections;

@Slf4j
@Configuration
public class UserReaperBatch {

    @Lazy
    @Autowired
    private JobRepository jobRepository;

    @Lazy
    @Autowired
    private CanceledUserRepository canceledUserRepository;

    @Lazy
    @Autowired
    private UserService userService;

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
                                       ItemWriter<CanceledUser> canceledUserWriter,
                                       AsyncTaskExecutor asyncTaskExecutor) {
        return new StepBuilder("deleteCanceledUsersStep", jobRepository)
                .<CanceledUser, CanceledUser>chunk(5)
                .reader(canceledUserReader)
                .processor(canceledUserProcessor)
                .writer(canceledUserWriter)
                .taskExecutor(asyncTaskExecutor) // Enable multi-threading
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
