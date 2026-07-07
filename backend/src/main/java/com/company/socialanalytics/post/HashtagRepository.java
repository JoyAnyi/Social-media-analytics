package com.company.socialanalytics.post;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HashtagRepository extends JpaRepository<Hashtag, UUID> {
    Optional<Hashtag> findByTag(String tag);

    @Query("select h.tag, count(p) from SocialPost p join p.hashtags h group by h.tag order by count(p) desc")
    java.util.List<Object[]> topHashtags(Pageable pageable);
}
