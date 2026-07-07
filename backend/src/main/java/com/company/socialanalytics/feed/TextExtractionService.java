package com.company.socialanalytics.feed;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class TextExtractionService {
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([A-Za-z][A-Za-z0-9_]{1,49})");
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z][A-Za-z0-9_]{1,49})");
    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9']+");
    private static final Set<String> STOP_WORDS = Set.of(
            "about", "again", "from", "into", "once", "that", "this", "with",
            "today", "latest", "really", "looks", "feels", "getting", "users",
            "update", "trying", "needs", "early", "clear"
    );

    public Set<String> extractHashtags(String content) {
        return extractMatches(content, HASHTAG_PATTERN);
    }

    public Set<String> extractMentions(String content) {
        return extractMatches(content, MENTION_PATTERN);
    }

    public Set<String> extractKeywords(String content) {
        Set<String> keywords = new LinkedHashSet<>();
        Matcher matcher = WORD_PATTERN.matcher(content == null ? "" : content);
        while (matcher.find() && keywords.size() < 12) {
            String term = matcher.group().toLowerCase(Locale.ROOT).replace("'", "");
            if (term.length() >= 4 && !STOP_WORDS.contains(term)) {
                keywords.add(term);
            }
        }
        return keywords;
    }

    private Set<String> extractMatches(String content, Pattern pattern) {
        Set<String> values = new LinkedHashSet<>();
        Matcher matcher = pattern.matcher(content == null ? "" : content);
        while (matcher.find()) {
            values.add(matcher.group(1).toLowerCase(Locale.ROOT));
        }
        return values;
    }
}
