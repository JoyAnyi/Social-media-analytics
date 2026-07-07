package com.company.socialanalytics.feed;

public interface PostEventPublisher {
    void publishRawPost(RawPostEvent event);
}
