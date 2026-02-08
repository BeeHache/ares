package net.blackhacker.ares;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "net.blackhacker.ares.repository.jpa")
public class JpaConfig {
}
