package com.company.socialanalytics.processing;

import com.company.socialanalytics.feed.SocialPlatform;
import com.company.socialanalytics.post.SentimentLabel;
import java.util.Set;

public record ProcessedPostEvent(
        String externalId,
        SocialPlatform platform,
        SentimentLabel sentiment,
        double sentimentScore,
        Set<String> hashtags,
        Set<String> mentions,
        Set<String> keywords
) {
    public ProcessedPostEvent {
        hashtags = hashtags == null ? Set.of() : Set.copyOf(hashtags);
        mentions = mentions == null ? Set.of() : Set.copyOf(mentions);
        keywords = keywords == null ? Set.of() : Set.copyOf(keywords);
    }
}
