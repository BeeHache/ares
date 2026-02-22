package net.blackhacker.ares;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.UserProvider;
import org.togglz.redis.RedisStateRepository;
import org.togglz.spring.security.SpringSecurityUserProvider;
import redis.clients.jedis.JedisPool;

@Configuration
public class AresTogglzConfig {

    @Bean
    public FeatureProvider featureProvider() {
        return new EnumBasedFeatureProvider(AresFeatures.class);
    }

    @Bean
    public StateRepository stateRepository(JedisPool jedisPool) {
        return new RedisStateRepository
                .Builder()
                .jedisPool(jedisPool)
                .keyPrefix("ares-feature:")
                .build();
    }

    @Bean
    public UserProvider userProvider() {
        return new SpringSecurityUserProvider("ADMIN");
    }
}
