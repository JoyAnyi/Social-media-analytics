package com.company.socialanalytics.security;

import java.util.Set;
import java.util.UUID;

public record CurrentUser(UUID id, String email, Set<String> authorities) {
}
