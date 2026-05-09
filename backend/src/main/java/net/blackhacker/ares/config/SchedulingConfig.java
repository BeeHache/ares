package net.blackhacker.ares.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    /**
     * Defines a central, explicit TaskScheduler for the entire application.
     * This prevents conflicts between @EnableScheduling and @EnableAsync,
     * ensuring that @Scheduled methods are always discovered and executed.
     *
     * @return A configured ThreadPoolTaskScheduler.
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("Scheduled-Task-");
        scheduler.initialize();
        return scheduler;
    }
}
