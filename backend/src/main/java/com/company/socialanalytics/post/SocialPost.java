package com.company.socialanalytics.post;

import com.company.socialanalytics.common.BaseEntity;
import com.company.socialanalytics.feed.SocialPlatform;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
public class SocialPost extends BaseEntity {
    @Column(nullable = false, unique = true, length = 120)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SocialPlatform platform;

    @Column(nullable = false, length = 120)
    private String authorUsername;

    @Column(nullable = false, length = 160)
    private String authorDisplayName;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false, length = 16)
    private String language;

    @Column(nullable = false)
    private Instant publishedAt;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private SentimentAnalysis sentimentAnalysis;

    @ManyToMany
    @JoinTable(
            name = "post_hashtags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private Set<Hashtag> hashtags = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "post_keywords",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    private Set<Keyword> keywords = new LinkedHashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostMention> mentions = new LinkedHashSet<>();

    protected SocialPost() {
    }

    public SocialPost(
            String externalId,
            SocialPlatform platform,
            String authorUsername,
            String authorDisplayName,
            String content,
            String language,
            Instant publishedAt
    ) {
        this.externalId = externalId;
        this.platform = platform;
        this.authorUsername = authorUsername;
        this.authorDisplayName = authorDisplayName;
        this.content = content;
        this.language = language;
        this.publishedAt = publishedAt;
    }

    public String getExternalId() {
        return externalId;
    }

    public SocialPlatform getPlatform() {
        return platform;
    }

    public String getContent() {
        return content;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public String getLanguage() {
        return language;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Set<Hashtag> getHashtags() {
        return hashtags;
    }

    public Set<Keyword> getKeywords() {
        return keywords;
    }

    public Set<PostMention> getMentions() {
        return mentions;
    }

    public SentimentAnalysis getSentimentAnalysis() {
        return sentimentAnalysis;
    }

    public void addHashtag(Hashtag hashtag) {
        hashtags.add(hashtag);
    }

    public void addKeyword(Keyword keyword) {
        keywords.add(keyword);
    }

    public void addMention(String username) {
        mentions.add(new PostMention(this, username));
    }

    public void setSentimentAnalysis(SentimentAnalysis sentimentAnalysis) {
        this.sentimentAnalysis = sentimentAnalysis;
    }
}
