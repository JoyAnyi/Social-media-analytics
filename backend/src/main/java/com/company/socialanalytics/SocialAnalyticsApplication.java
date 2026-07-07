package com.company.socialanalytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SocialAnalyticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SocialAnalyticsApplication.class, args);
    }
}
