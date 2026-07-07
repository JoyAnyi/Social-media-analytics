package com.company.socialanalytics.post;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SentimentAnalysisRepository extends JpaRepository<SentimentAnalysis, UUID> {
    @Query("select s.label, count(s), avg(s.score) from SentimentAnalysis s group by s.label")
    List<Object[]> summarizeSentiment();
}
