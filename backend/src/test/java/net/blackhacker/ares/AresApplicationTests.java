package net.blackhacker.ares;

import net.blackhacker.ares.service.RoleHierarchyService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.batch.autoconfigure.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(
    properties = {
            "spring.batch.job.name=feedUpdateJob",
            "spring.main.allow-bean-definition-overriding=true"
    }
)
@EnableAutoConfiguration(exclude = {BatchAutoConfiguration.class})
@Import({AresApplicationTests.TestSecurityConfiguration.class, TestConfig.class})
class AresApplicationTests extends AbstractIntegrationTest {

    @TestConfiguration
    static class TestSecurityConfiguration {
        @Bean
        @Primary
        public RoleHierarchyService roleHierarchyService() {
            RoleHierarchyService mockService = mock(RoleHierarchyService.class);
            when(mockService.getRoleHierarchyString()).thenReturn("ROLE_ADMIN > ROLE_USER");
            return mockService;
        }
    }

    @Test
    void contextLoads() {
        // Context should now load successfully without Batch components interfering
    }
}
