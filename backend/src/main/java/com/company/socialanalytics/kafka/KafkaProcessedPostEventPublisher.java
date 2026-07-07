package com.company.socialanalytics.kafka;

import com.company.socialanalytics.processing.ProcessedPostEvent;
import com.company.socialanalytics.processing.ProcessedPostEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
class KafkaProcessedPostEventPublisher implements ProcessedPostEventPublisher {
    private final KafkaTemplate<String, ProcessedPostEvent> kafkaTemplate;
    private final AppKafkaProperties kafkaProperties;

    KafkaProcessedPostEventPublisher(
            KafkaTemplate<String, ProcessedPostEvent> kafkaTemplate,
            AppKafkaProperties kafkaProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    @Override
    public void publish(ProcessedPostEvent event) {
        kafkaTemplate.send(kafkaProperties.getProcessedPostTopic(), event.externalId(), event);
    }
}
