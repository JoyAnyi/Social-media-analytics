package com.company.socialanalytics.dashboard;

import com.company.socialanalytics.feed.SocialPlatform;
import com.company.socialanalytics.post.SentimentLabel;
import java.time.Instant;

public record LatestPostView(
        String externalId,
        SocialPlatform platform,
        String authorUsername,
        String authorDisplayName,
        String content,
        String language,
        SentimentLabel sentiment,
        Instant publishedAt
) {
}
