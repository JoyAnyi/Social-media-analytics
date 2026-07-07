package com.company.socialanalytics.user;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String username,
        String displayName,
        Set<RoleName> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
