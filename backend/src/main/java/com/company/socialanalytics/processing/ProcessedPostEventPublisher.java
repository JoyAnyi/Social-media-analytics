package com.company.socialanalytics.processing;

public interface ProcessedPostEventPublisher {
    void publish(ProcessedPostEvent event);
}
