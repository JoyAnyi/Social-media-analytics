package com.company.socialanalytics.processing;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.socialanalytics.feed.RawPostEvent;
import com.company.socialanalytics.feed.SocialPlatform;
import com.company.socialanalytics.post.SentimentLabel;
import com.company.socialanalytics.post.SocialPostRepository;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PostProcessingServiceIntegrationTest {
    @Autowired
    private PostProcessingService postProcessingService;

    @Autowired
    private SocialPostRepository socialPostRepository;

    @Test
    void processesRawPostIntoPersistedEnrichment() {
        RawPostEvent event = new RawPostEvent(
                "external-processor-test-1",
                SocialPlatform.X,
                "launchdesk",
                "Launch Desk",
                "Great momentum for the new analytics dashboard #LaunchDay @marketwatcher",
                Set.of("launchday"),
                Set.of("marketwatcher"),
                "en",
                Instant.parse("2026-07-06T12:00:00Z")
        );

        ProcessedPostResponse response = postProcessingService.process(event);
        ProcessedPostResponse duplicate = postProcessingService.process(event);

        assertThat(response.externalId()).isEqualTo(event.externalId());
        assertThat(response.sentiment()).isEqualTo(SentimentLabel.POSITIVE);
        assertThat(response.hashtags()).contains("launchday");
        assertThat(response.mentions()).contains("marketwatcher");
        assertThat(response.keywords()).contains("analytics", "dashboard", "momentum");
        assertThat(duplicate.externalId()).isEqualTo(response.externalId());
        assertThat(socialPostRepository.findWithDetailsByExternalId(event.externalId())).isPresent();
        assertThat(socialPostRepository.countByExternalId(event.externalId())).isEqualTo(1);
    }

    @Test
    void rejectsInvalidRawPostEvents() {
        RawPostEvent event = new RawPostEvent(
                "",
                SocialPlatform.X,
                "launchdesk",
                "Launch Desk",
                "",
                Set.of(),
                Set.of(),
                "en",
                Instant.parse("2026-07-06T12:00:00Z")
        );

        assertThat(org.assertj.core.api.Assertions.catchThrowable(() -> postProcessingService.process(event)))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
