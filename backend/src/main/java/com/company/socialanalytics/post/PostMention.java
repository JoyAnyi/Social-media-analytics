package com.company.socialanalytics.post;

import com.company.socialanalytics.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "post_mentions")
public class PostMention extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private SocialPost post;

    @Column(nullable = false, length = 80)
    private String username;

    protected PostMention() {
    }

    public PostMention(SocialPost post, String username) {
        this.post = post;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
