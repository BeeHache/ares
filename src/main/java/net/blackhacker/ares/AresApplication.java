package net.blackhacker.ares;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableScheduling
@EnableJms
@EnableCaching
public class AresApplication implements WebMvcConfigurer {


    public static void main(String[] args) {
        SpringApplication.run(AresApplication.class, args);
    }
}
