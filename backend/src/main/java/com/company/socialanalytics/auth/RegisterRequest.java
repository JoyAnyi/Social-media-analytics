package com.company.socialanalytics.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank
        @Size(min = 3, max = 80)
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "must contain only letters, numbers, dots, dashes, and underscores")
        String username,
        @NotBlank @Size(min = 2, max = 160) String displayName,
        @NotBlank @Size(min = 12, max = 128) String password
) {
}
