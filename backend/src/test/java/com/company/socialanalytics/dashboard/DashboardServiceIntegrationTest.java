package com.company.socialanalytics.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.socialanalytics.feed.RawPostEvent;
import com.company.socialanalytics.feed.SocialPlatform;
import com.company.socialanalytics.processing.PostProcessingService;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DashboardServiceIntegrationTest {
    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private PostProcessingService postProcessingService;

    @Test
    void summarizesPersistedPostsForLiveDashboard() {
        postProcessingService.process(new RawPostEvent(
                "dashboard-summary-test-1",
                SocialPlatform.LINKEDIN,
                "opslead",
                "Ops Lead",
                "Great launch momentum for analytics workflows #LaunchDay @marketwatcher",
                Set.of("launchday"),
                Set.of("marketwatcher"),
                "en",
                Instant.now()
        ));

        DashboardSummary summary = dashboardService.summary();

        assertThat(summary.totalPosts()).isGreaterThanOrEqualTo(1);
        assertThat(summary.postsToday()).isGreaterThanOrEqualTo(1);
        assertThat(summary.sentiment().positive()).isGreaterThanOrEqualTo(1);
        assertThat(summary.topHashtags()).extracting(MetricPoint::label).contains("launchday");
        assertThat(summary.topKeywords()).extracting(MetricPoint::label).contains("analytics");
        assertThat(summary.activePlatforms()).extracting(MetricPoint::label).contains("LINKEDIN");
        assertThat(summary.latestPosts()).extracting(LatestPostView::externalId).contains("dashboard-summary-test-1");
        assertThat(summary.systemHealth().database()).isEqualTo("UP");
        assertThat(summary.systemHealth().kafka()).isEqualTo("DISABLED");
    }
}
