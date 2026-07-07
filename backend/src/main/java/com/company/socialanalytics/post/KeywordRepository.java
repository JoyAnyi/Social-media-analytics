package com.company.socialanalytics.post;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KeywordRepository extends JpaRepository<Keyword, UUID> {
    Optional<Keyword> findByTerm(String term);

    @Query("select k.term, count(p) from SocialPost p join p.keywords k group by k.term order by count(p) desc")
    java.util.List<Object[]> topKeywords(Pageable pageable);
}
