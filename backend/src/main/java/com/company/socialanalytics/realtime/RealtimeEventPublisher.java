package com.company.socialanalytics.realtime;

public interface RealtimeEventPublisher {
    void publish(RealtimeChannel channel, Object payload);
}
