package com.company.socialanalytics.post;

import com.company.socialanalytics.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "keywords")
public class Keyword extends BaseEntity {
    @Column(nullable = false, unique = true, length = 120)
    private String term;

    protected Keyword() {
    }

    public Keyword(String term) {
        this.term = term;
    }

    public String getTerm() {
        return term;
    }
}
