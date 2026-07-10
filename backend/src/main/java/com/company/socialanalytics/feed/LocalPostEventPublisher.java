package com.company.socialanalytics.feed;

import com.company.socialanalytics.processing.PostProcessingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "false")
class LocalPostEventPublisher implements PostEventPublisher {
    private final PostProcessingService postProcessingService;

    LocalPostEventPublisher(PostProcessingService postProcessingService) {
        this.postProcessingService = postProcessingService;
    }

    @Override
    public void publishRawPost(RawPostEvent event) {
        postProcessingService.process(event);
    }
}
