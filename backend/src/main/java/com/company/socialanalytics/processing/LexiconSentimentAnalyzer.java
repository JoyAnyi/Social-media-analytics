package com.company.socialanalytics.processing;

import com.company.socialanalytics.post.SentimentLabel;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
class LexiconSentimentAnalyzer implements SentimentAnalyzer {
    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9']+");
    private static final Set<String> POSITIVE_WORDS = Set.of(
            "clear", "excellent", "fast", "great", "love", "momentum", "strong", "useful"
    );
    private static final Set<String> NEGATIVE_WORDS = Set.of(
            "confusing", "not", "painful", "slow", "support", "watchlist"
    );

    @Override
    public SentimentResult analyze(String content) {
        int tokens = 0;
        int positiveHits = 0;
        int negativeHits = 0;
        Matcher matcher = WORD_PATTERN.matcher(content == null ? "" : content);
        while (matcher.find()) {
            tokens++;
            String word = matcher.group().toLowerCase(Locale.ROOT).replace("'", "");
            if (POSITIVE_WORDS.contains(word)) {
                positiveHits++;
            }
            if (NEGATIVE_WORDS.contains(word)) {
                negativeHits++;
            }
        }
        int denominator = Math.max(tokens, 1);
        double score = (positiveHits - negativeHits) / (double) denominator;
        SentimentLabel label = labelFor(score);
        return new SentimentResult(label, score, positiveHits, negativeHits);
    }

    private SentimentLabel labelFor(double score) {
        if (score >= 0.08d) {
            return SentimentLabel.POSITIVE;
        }
        if (score <= -0.08d) {
            return SentimentLabel.NEGATIVE;
        }
        return SentimentLabel.NEUTRAL;
    }
}
