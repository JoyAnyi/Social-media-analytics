package com.company.socialanalytics.post;

import com.company.socialanalytics.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "hashtags")
public class Hashtag extends BaseEntity {
    @Column(nullable = false, unique = true, length = 80)
    private String tag;

    protected Hashtag() {
    }

    public Hashtag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
