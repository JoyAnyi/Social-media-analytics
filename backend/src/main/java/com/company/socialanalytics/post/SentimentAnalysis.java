package com.company.socialanalytics.post;

import com.company.socialanalytics.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sentiment_analyses")
public class SentimentAnalysis extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private SocialPost post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private SentimentLabel label;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private int positiveHits;

    @Column(nullable = false)
    private int negativeHits;

    protected SentimentAnalysis() {
    }

    public SentimentAnalysis(
            SocialPost post,
            SentimentLabel label,
            double score,
            int positiveHits,
            int negativeHits
    ) {
        this.post = post;
        this.label = label;
        this.score = score;
        this.positiveHits = positiveHits;
        this.negativeHits = negativeHits;
    }

    public SentimentLabel getLabel() {
        return label;
    }

    public double getScore() {
        return score;
    }

    public int getPositiveHits() {
        return positiveHits;
    }

    public int getNegativeHits() {
        return negativeHits;
    }
}
