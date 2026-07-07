package com.company.socialanalytics.feed;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feed")
public class FeedSimulatorController {
    private final FeedSimulatorService feedSimulatorService;

    public FeedSimulatorController(FeedSimulatorService feedSimulatorService) {
        this.feedSimulatorService = feedSimulatorService;
    }

    @PostMapping("/simulator/posts")
    ResponseEntity<GeneratedPostResponse> generatePosts(@Valid @RequestBody GeneratePostsRequest request) {
        return ResponseEntity.ok(new GeneratedPostResponse(feedSimulatorService.generateAndPublish(request)));
    }
}
