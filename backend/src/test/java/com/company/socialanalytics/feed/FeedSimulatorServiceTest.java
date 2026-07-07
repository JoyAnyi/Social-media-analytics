package com.company.socialanalytics.feed;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class FeedSimulatorServiceTest {
    @Test
    void generatesPostsAndPublishesEachEvent() {
        CapturingPublisher publisher = new CapturingPublisher();
        FeedSimulatorService service = new FeedSimulatorService(
                publisher,
                new TextExtractionService(),
                Clock.fixed(Instant.parse("2026-07-06T12:00:00Z"), ZoneOffset.UTC)
        );

        List<RawPostEvent> posts = service.generateAndPublish(new GeneratePostsRequest(2, null, "AI Commerce"));

        assertThat(posts).hasSize(2);
        assertThat(publisher.events).containsExactlyElementsOf(posts);
        assertThat(posts.getFirst().content()).contains("AI Commerce");
        assertThat(posts.getFirst().hashtags()).contains("aicommerce");
        assertThat(posts.getFirst().mentions()).contains("trendpilot");
        assertThat(posts.getFirst().publishedAt()).isEqualTo(Instant.parse("2026-07-06T12:00:00Z"));
    }

    private static class CapturingPublisher implements PostEventPublisher {
        private final List<RawPostEvent> events = new ArrayList<>();

        @Override
        public void publishRawPost(RawPostEvent event) {
            events.add(event);
        }
    }
}
