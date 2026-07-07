package com.company.socialanalytics.dashboard;

import java.time.Instant;
import java.util.List;

public record DashboardSummary(
        long totalPosts,
        long postsToday,
        long postsPerMinute,
        long postsPerSecond,
        SentimentBreakdown sentiment,
        List<MetricPoint> topHashtags,
        List<MetricPoint> topKeywords,
        List<MetricPoint> activePlatforms,
        List<MetricPoint> topUsers,
        List<LatestPostView> latestPosts,
        SystemHealthView systemHealth,
        Instant generatedAt
) {
}
