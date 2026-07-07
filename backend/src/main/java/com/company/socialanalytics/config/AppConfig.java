package com.company.socialanalytics.config;

import com.company.socialanalytics.kafka.AppKafkaProperties;
import com.company.socialanalytics.realtime.WebSocketProperties;
import com.company.socialanalytics.security.SecurityProperties;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AppKafkaProperties.class, WebSocketProperties.class, SecurityProperties.class})
public class AppConfig {
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
