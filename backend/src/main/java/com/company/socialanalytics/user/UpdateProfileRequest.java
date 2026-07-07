package com.company.socialanalytics.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank
        @Size(min = 3, max = 80)
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "must contain only letters, numbers, dots, dashes, and underscores")
        String username,

        @NotBlank
        @Size(min = 2, max = 160)
        String displayName
) {
}
