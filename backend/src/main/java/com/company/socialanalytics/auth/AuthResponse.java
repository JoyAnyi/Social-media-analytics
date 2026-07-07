package com.company.socialanalytics.auth;

import com.company.socialanalytics.user.UserResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        UserResponse user
) {
}
