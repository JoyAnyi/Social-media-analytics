package com.company.socialanalytics.feed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

public record RawPostEvent(
        @NotBlank @Size(max = 120) String externalId,
        @NotNull SocialPlatform platform,
        @NotBlank @Size(max = 120) String authorUsername,
        @NotBlank @Size(max = 160) String authorDisplayName,
        @NotBlank @Size(max = 2000) String content,
        Set<String> hashtags,
        Set<String> mentions,
        @NotBlank @Size(max = 16) String language,
        @NotNull Instant publishedAt
) {
    public RawPostEvent {
        hashtags = hashtags == null ? Set.of() : Set.copyOf(hashtags);
        mentions = mentions == null ? Set.of() : Set.copyOf(mentions);
    }
}
