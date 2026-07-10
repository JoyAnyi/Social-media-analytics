package com.company.socialanalytics.kafka;

import com.company.socialanalytics.feed.PostEventPublisher;
import com.company.socialanalytics.feed.RawPostEvent;
import com.company.socialanalytics.common.ServiceUnavailableException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
class KafkaPostEventPublisher implements PostEventPublisher {
    private final KafkaTemplate<String, RawPostEvent> kafkaTemplate;
    private final AppKafkaProperties kafkaProperties;

    KafkaPostEventPublisher(KafkaTemplate<String, RawPostEvent> kafkaTemplate, AppKafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    @Override
    public void publishRawPost(RawPostEvent event) {
        try {
            kafkaTemplate.send(kafkaProperties.getRawPostTopic(), event.externalId(), event)
                    .orTimeout(10, TimeUnit.SECONDS)
                    .join();
        } catch (CompletionException ex) {
            throw new ServiceUnavailableException("Kafka did not accept the simulated post. Confirm Kafka is running and the posts.raw topic exists.");
        }
    }
}
