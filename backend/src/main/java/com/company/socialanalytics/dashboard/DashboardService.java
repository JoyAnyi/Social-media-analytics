package com.company.socialanalytics.dashboard;

import com.company.socialanalytics.post.HashtagRepository;
import com.company.socialanalytics.post.KeywordRepository;
import com.company.socialanalytics.post.SentimentAnalysisRepository;
import com.company.socialanalytics.post.SentimentLabel;
import com.company.socialanalytics.post.SocialPost;
import com.company.socialanalytics.post.SocialPostRepository;
import com.company.socialanalytics.system.SystemHealthService;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final SocialPostRepository socialPostRepository;
    private final SentimentAnalysisRepository sentimentAnalysisRepository;
    private final HashtagRepository hashtagRepository;
    private final KeywordRepository keywordRepository;
    private final SystemHealthService systemHealthService;
    private final Clock clock;

    public DashboardService(
            SocialPostRepository socialPostRepository,
            SentimentAnalysisRepository sentimentAnalysisRepository,
            HashtagRepository hashtagRepository,
            KeywordRepository keywordRepository,
            SystemHealthService systemHealthService,
            Clock clock
    ) {
        this.socialPostRepository = socialPostRepository;
        this.sentimentAnalysisRepository = sentimentAnalysisRepository;
        this.hashtagRepository = hashtagRepository;
        this.keywordRepository = keywordRepository;
        this.systemHealthService = systemHealthService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardSummary summary() {
        return summary(true);
    }

    @Transactional(readOnly = true)
    public DashboardSummary summary(boolean includeSystemHealth) {
        Instant now = clock.instant();
        long totalPosts = socialPostRepository.count();
        long postsToday = socialPostRepository.countByPublishedAtAfter(now.truncatedTo(ChronoUnit.DAYS));
        long postsPerMinute = socialPostRepository.countByPublishedAtAfter(now.minus(1, ChronoUnit.MINUTES));
        long postsPerSecond = socialPostRepository.countByPublishedAtAfter(now.minus(1, ChronoUnit.SECONDS));
        return new DashboardSummary(
                totalPosts,
                postsToday,
                postsPerMinute,
                postsPerSecond,
                sentimentBreakdown(),
                topMetrics(hashtagRepository.topHashtags(PageRequest.of(0, 8))),
                topMetrics(keywordRepository.topKeywords(PageRequest.of(0, 8))),
                topMetrics(socialPostRepository.countByPlatform()),
                topMetrics(socialPostRepository.topAuthors(PageRequest.of(0, 8))),
                latestPosts(),
                includeSystemHealth ? systemHealthService.snapshot() : null,
                now
        );
    }

    private SentimentBreakdown sentimentBreakdown() {
        Map<SentimentLabel, Long> counts = new EnumMap<>(SentimentLabel.class);
        double weightedScore = 0;
        long total = 0;
        for (Object[] row : sentimentAnalysisRepository.summarizeSentiment()) {
            SentimentLabel label = (SentimentLabel) row[0];
            long count = (Long) row[1];
            double average = row[2] == null ? 0 : ((Number) row[2]).doubleValue();
            counts.put(label, count);
            total += count;
            weightedScore += average * count;
        }
        return new SentimentBreakdown(
                counts.getOrDefault(SentimentLabel.POSITIVE, 0L),
                counts.getOrDefault(SentimentLabel.NEUTRAL, 0L),
                counts.getOrDefault(SentimentLabel.NEGATIVE, 0L),
                total == 0 ? 0 : weightedScore / total
        );
    }

    private List<LatestPostView> latestPosts() {
        return socialPostRepository.findLatestWithSentiment(PageRequest.of(0, 10)).stream()
                .map(this::toLatestPost)
                .toList();
    }

    private LatestPostView toLatestPost(SocialPost post) {
        return new LatestPostView(
                post.getExternalId(),
                post.getPlatform(),
                post.getAuthorUsername(),
                post.getAuthorDisplayName(),
                post.getContent(),
                post.getLanguage(),
                post.getSentimentAnalysis().getLabel(),
                post.getPublishedAt()
        );
    }

    private List<MetricPoint> topMetrics(List<Object[]> rows) {
        return rows.stream()
                .map(row -> new MetricPoint(String.valueOf(row[0]), ((Number) row[1]).longValue()))
                .toList();
    }
}
