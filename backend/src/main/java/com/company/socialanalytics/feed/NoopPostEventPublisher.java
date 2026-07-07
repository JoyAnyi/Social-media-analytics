package com.company.socialanalytics.feed;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "false")
class NoopPostEventPublisher implements PostEventPublisher {
    @Override
    public void publishRawPost(RawPostEvent event) {
        // Kafka can be disabled for tests and local backend-only development.
    }
}
