package com.company.socialanalytics.post;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SocialPostRepository extends JpaRepository<SocialPost, UUID> {
    boolean existsByExternalId(String externalId);

    @EntityGraph(attributePaths = {"hashtags", "keywords", "mentions", "sentimentAnalysis"})
    Optional<SocialPost> findWithDetailsByExternalId(String externalId);

    long countByPublishedAtAfter(java.time.Instant publishedAt);

    @Query("select p.platform, count(p) from SocialPost p group by p.platform")
    java.util.List<Object[]> countByPlatform();

    @Query("select p.authorUsername, count(p) from SocialPost p group by p.authorUsername order by count(p) desc")
    java.util.List<Object[]> topAuthors(org.springframework.data.domain.Pageable pageable);

    @Query("select p from SocialPost p left join fetch p.sentimentAnalysis order by p.publishedAt desc")
    java.util.List<SocialPost> findLatestWithSentiment(org.springframework.data.domain.Pageable pageable);
}
