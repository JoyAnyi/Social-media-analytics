package com.company.socialanalytics.kafka;

import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
class KafkaTopicConfig {
    @Bean
    NewTopic[] socialAnalyticsTopics(AppKafkaProperties properties) {
        Set<String> topicNames = new LinkedHashSet<>();
        topicNames.add(properties.getRawPostTopic());
        topicNames.add("posts.validated");
        topicNames.add(properties.getProcessedPostTopic());
        topicNames.add("analytics.sentiment");
        topicNames.add("analytics.keyword");
        topicNames.add("analytics.hashtag");
        topicNames.add("analytics.completed");
        topicNames.add("notifications.created");
        topicNames.add("reports.generated");
        topicNames.add("audit.created");

        return topicNames.stream()
                .map(topic -> TopicBuilder.name(topic).partitions(3).replicas(1).build())
                .toArray(NewTopic[]::new);
    }
}
