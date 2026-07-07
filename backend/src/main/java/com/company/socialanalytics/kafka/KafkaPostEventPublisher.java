package com.company.socialanalytics.kafka;

import com.company.socialanalytics.feed.PostEventPublisher;
import com.company.socialanalytics.feed.RawPostEvent;
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
        kafkaTemplate.send(kafkaProperties.getRawPostTopic(), event.externalId(), event);
    }
}
