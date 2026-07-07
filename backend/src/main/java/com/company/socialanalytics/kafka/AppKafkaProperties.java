package com.company.socialanalytics.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public class AppKafkaProperties {
    private boolean enabled = true;
    private String rawPostTopic = "social.raw-posts";
    private String processedPostTopic = "social.processed-posts";
    private String consumerGroupId = "socialanalytics-post-processing";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRawPostTopic() {
        return rawPostTopic;
    }

    public void setRawPostTopic(String rawPostTopic) {
        this.rawPostTopic = rawPostTopic;
    }

    public String getProcessedPostTopic() {
        return processedPostTopic;
    }

    public void setProcessedPostTopic(String processedPostTopic) {
        this.processedPostTopic = processedPostTopic;
    }

    public String getConsumerGroupId() {
        return consumerGroupId;
    }

    public void setConsumerGroupId(String consumerGroupId) {
        this.consumerGroupId = consumerGroupId;
    }
}
