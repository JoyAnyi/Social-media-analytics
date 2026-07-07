package com.company.socialanalytics.feed;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record GeneratePostsRequest(
        @Min(1) @Max(50) Integer count,
        SimulationSpeed speed,
        @Size(max = 80) String topic
) {
    int resolvedCount() {
        if (count != null) {
            return count;
        }
        return speed == null ? SimulationSpeed.MEDIUM.defaultBatchSize() : speed.defaultBatchSize();
    }

    String resolvedTopic() {
        if (topic == null || topic.isBlank()) {
            return "brand launch";
        }
        return topic.strip();
    }
}
