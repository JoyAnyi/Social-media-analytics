package com.company.socialanalytics.kafka;

import com.company.socialanalytics.feed.RawPostEvent;
import com.company.socialanalytics.processing.PostProcessingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
class PostProcessingKafkaListener {
    private final PostProcessingService postProcessingService;

    PostProcessingKafkaListener(PostProcessingService postProcessingService) {
        this.postProcessingService = postProcessingService;
    }

    @KafkaListener(
            topics = "${app.kafka.raw-post-topic}",
            groupId = "${app.kafka.consumer-group-id}"
    )
    void consume(RawPostEvent event) {
        postProcessingService.process(event);
    }
}
