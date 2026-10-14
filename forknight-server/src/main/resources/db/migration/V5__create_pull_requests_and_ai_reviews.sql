-- ============================================================
-- Migration V5: Pull Requests and AI Code Reviews
-- ============================================================

CREATE TABLE pull_requests (
    id              BIGSERIAL       PRIMARY KEY,
    repo_id         BIGINT          NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    author_id       BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(255)    NOT NULL,
    description     TEXT,
    source_branch   VARCHAR(100)    NOT NULL,
    target_branch   VARCHAR(100)    NOT NULL DEFAULT 'main',
    status          VARCHAR(20)     NOT NULL DEFAULT 'OPEN',
    files_changed   INTEGER         NOT NULL DEFAULT 0,
    additions       INTEGER         NOT NULL DEFAULT 0,
    deletions       INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pr_repo ON pull_requests(repo_id, status);
CREATE INDEX idx_pr_author ON pull_requests(author_id);

CREATE TABLE ai_reviews (
    id              BIGSERIAL       PRIMARY KEY,
    pull_request_id BIGINT          REFERENCES pull_requests(id) ON DELETE CASCADE,
    summary         TEXT            NOT NULL,
    review_body     TEXT,
    score           INTEGER         NOT NULL CHECK (score >= 1 AND score <= 10),
    issues_json     TEXT,
    model_used      VARCHAR(50)     NOT NULL,
    tokens_used     INTEGER,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_review_pr ON ai_reviews(pull_request_id);
