package com.company.socialanalytics.processing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "false")
class NoopProcessedPostEventPublisher implements ProcessedPostEventPublisher {
    @Override
    public void publish(ProcessedPostEvent event) {
        // Kafka can be disabled in tests and local backend-only development.
    }
}
