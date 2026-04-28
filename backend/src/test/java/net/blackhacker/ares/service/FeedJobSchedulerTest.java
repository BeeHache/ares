package net.blackhacker.ares.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedJobSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job feedUpdateJob;

    private FeedJobScheduler scheduler;

    private final Long intervalMs = 300000L;

    @BeforeEach
    void setUp() {
        scheduler = new FeedJobScheduler(jobLauncher, feedUpdateJob, intervalMs);
    }

    @Test
    void runFeedUpdateJob_shouldLaunchJobWithParameters() throws Exception {
        // Act
        scheduler.runFeedUpdateJob();

        // Assert
        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(feedUpdateJob), paramsCaptor.capture());

        JobParameters params = paramsCaptor.getValue();
        assertNotNull(params.getString("threshold"));
        assertNotNull(params.getLong("time"));
    }

    @Test
    void runFeedUpdateJob_shouldHandleExceptionGracefully() throws Exception {
        // Arrange
        when(jobLauncher.run(any(), any())).thenThrow(new RuntimeException("Launch failed"));

        // Act & Assert (Should not throw exception)
        assertDoesNotThrow(() -> scheduler.runFeedUpdateJob());
        verify(jobLauncher).run(any(), any());
    }
}
