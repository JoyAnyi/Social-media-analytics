package com.company.socialanalytics.processing;

import com.company.socialanalytics.post.SentimentLabel;

public record SentimentResult(
        SentimentLabel label,
        double score,
        int positiveHits,
        int negativeHits
) {
}
