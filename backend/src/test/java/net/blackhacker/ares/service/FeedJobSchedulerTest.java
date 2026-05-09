package net.blackhacker.ares.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedJobSchedulerTest {

    @Mock
    private JobOperator jobOperator;

    @Mock
    private Job feedUpdateJob;

    private FeedJobScheduler scheduler;

    private final Long intervalMs = 300000L;

    @BeforeEach
    void setUp() {
        scheduler = new FeedJobScheduler(jobOperator, feedUpdateJob, intervalMs);
    }

    @Test
    void runFeedUpdateJob_shouldLaunchJobWithCorrectParameters() throws Exception {

        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        scheduler.runFeedUpdateJob();
        verify(jobOperator).start(eq(feedUpdateJob), paramsCaptor.capture());

        // Then you extract the value AFTER the verify method has captured it
        JobParameters params = paramsCaptor.getValue();
        assertNotNull(params.getString("threshold"));
        assertNotNull(params.getLong("time"));
    }

    @Test
    void runFeedUpdateJob_shouldHandleExceptionGracefully() throws Exception {
        // Arrange
        when(jobOperator.start(eq(feedUpdateJob), any(JobParameters.class))).thenThrow(new RuntimeException("Launch failed"));

        // Act & Assert (Should not throw exception)
        assertDoesNotThrow(() -> scheduler.runFeedUpdateJob());
    }
}
