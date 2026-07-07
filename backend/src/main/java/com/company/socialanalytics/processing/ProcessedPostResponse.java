package com.company.socialanalytics.processing;

import com.company.socialanalytics.feed.SocialPlatform;
import com.company.socialanalytics.post.SentimentLabel;
import java.util.Set;

public record ProcessedPostResponse(
        String externalId,
        SocialPlatform platform,
        SentimentLabel sentiment,
        double sentimentScore,
        Set<String> hashtags,
        Set<String> mentions,
        Set<String> keywords
) {
}
