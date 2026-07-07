CREATE TABLE posts (
    id UUID PRIMARY KEY,
    external_id VARCHAR(120) NOT NULL UNIQUE,
    platform VARCHAR(32) NOT NULL,
    author_username VARCHAR(120) NOT NULL,
    author_display_name VARCHAR(160) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    language VARCHAR(16) NOT NULL,
    published_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE sentiment_analyses (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL UNIQUE REFERENCES posts(id) ON DELETE CASCADE,
    label VARCHAR(24) NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    positive_hits INTEGER NOT NULL,
    negative_hits INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE hashtags (
    id UUID PRIMARY KEY,
    tag VARCHAR(80) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE keywords (
    id UUID PRIMARY KEY,
    term VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE post_mentions (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    username VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE post_hashtags (
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    hashtag_id UUID NOT NULL REFERENCES hashtags(id) ON DELETE RESTRICT,
    PRIMARY KEY (post_id, hashtag_id)
);

CREATE TABLE post_keywords (
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    keyword_id UUID NOT NULL REFERENCES keywords(id) ON DELETE RESTRICT,
    PRIMARY KEY (post_id, keyword_id)
);

CREATE INDEX idx_posts_platform_published_at ON posts(platform, published_at DESC);
CREATE INDEX idx_posts_author_username ON posts(author_username);
CREATE INDEX idx_sentiment_analyses_label ON sentiment_analyses(label);
CREATE INDEX idx_post_mentions_username ON post_mentions(username);
CREATE INDEX idx_post_hashtags_hashtag_id ON post_hashtags(hashtag_id);
CREATE INDEX idx_post_keywords_keyword_id ON post_keywords(keyword_id);
