package com.company.socialanalytics.feed;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.socialanalytics.post.SocialPostRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FeedSimulatorIntegrationTest {
    @Autowired
    private FeedSimulatorService feedSimulatorService;

    @Autowired
    private SocialPostRepository socialPostRepository;

    @Test
    void persistsGeneratedPostsWhenKafkaIsDisabled() {
        long before = socialPostRepository.count();

        List<RawPostEvent> generatedPosts = feedSimulatorService.generateAndPublish(new GeneratePostsRequest(3, null, "Local Demo"));

        assertThat(socialPostRepository.count()).isEqualTo(before + 3);
        assertThat(generatedPosts)
                .allSatisfy(event -> {
                    var post = socialPostRepository.findWithDetailsByExternalId(event.externalId()).orElseThrow();
                    assertThat(post.getContent()).contains("Local Demo");
                    assertThat(post.getSentimentAnalysis()).isNotNull();
                });
    }
}
