package com.company.socialanalytics.realtime;

import java.time.Instant;

public record RealtimeMessage(String channel, Object payload, Instant timestamp) {
}
