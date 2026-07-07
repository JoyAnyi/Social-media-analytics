package com.company.socialanalytics.feed;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class FeedSimulatorService {
    private static final List<SocialPlatform> PLATFORMS = List.of(
            SocialPlatform.X,
            SocialPlatform.INSTAGRAM,
            SocialPlatform.TIKTOK,
            SocialPlatform.LINKEDIN,
            SocialPlatform.REDDIT,
            SocialPlatform.YOUTUBE
    );
    private static final List<String> AUTHORS = List.of(
            "marketwatcher",
            "trendpilot",
            "communitylead",
            "launchdesk",
            "brandops"
    );
    private static final List<String> TEMPLATES = List.of(
            "Really love the {topic} update. The rollout feels fast, useful, and surprisingly clear.",
            "Not convinced by {topic} yet. The latest demo felt slow and confusing.",
            "{topic} is getting attention again. Useful features, but support needs to improve.",
            "The community response to {topic} looks strong today. Great momentum from early users.",
            "Trying {topic} now. Setup was painful, but the dashboard is excellent once it loads."
    );

    private final PostEventPublisher postEventPublisher;
    private final TextExtractionService textExtractionService;
    private final Clock clock;
    private final AtomicInteger cursor = new AtomicInteger();

    public FeedSimulatorService(
            PostEventPublisher postEventPublisher,
            TextExtractionService textExtractionService,
            Clock clock
    ) {
        this.postEventPublisher = postEventPublisher;
        this.textExtractionService = textExtractionService;
        this.clock = clock;
    }

    public List<RawPostEvent> generateAndPublish(GeneratePostsRequest request) {
        int count = request.resolvedCount();
        String topic = request.resolvedTopic();
        List<RawPostEvent> events = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            RawPostEvent event = generate(topic);
            postEventPublisher.publishRawPost(event);
            events.add(event);
        }
        return events;
    }

    private RawPostEvent generate(String topic) {
        int value = cursor.getAndIncrement();
        String topicSlug = topic.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
        String author = AUTHORS.get(value % AUTHORS.size());
        String content = TEMPLATES.get(value % TEMPLATES.size()).replace("{topic}", topic)
                + " #" + topicSlug
                + " #" + sentimentTag(value)
                + " @" + AUTHORS.get((value + 1) % AUTHORS.size());
        Set<String> hashtags = textExtractionService.extractHashtags(content);
        Set<String> mentions = textExtractionService.extractMentions(content);
        Instant publishedAt = clock.instant().minusSeconds(value * 11L);

        return new RawPostEvent(
                "sim-" + UUID.randomUUID(),
                PLATFORMS.get(value % PLATFORMS.size()),
                author,
                displayName(author),
                content,
                hashtags,
                mentions,
                "en",
                publishedAt
        );
    }

    private String sentimentTag(int value) {
        return value % 3 == 1 ? "watchlist" : "momentum";
    }

    private String displayName(String username) {
        return switch (username) {
            case "marketwatcher" -> "Market Watcher";
            case "trendpilot" -> "Trend Pilot";
            case "communitylead" -> "Community Lead";
            case "launchdesk" -> "Launch Desk";
            default -> "Brand Ops";
        };
    }
}
