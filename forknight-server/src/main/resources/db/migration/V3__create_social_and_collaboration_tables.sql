-- ============================================================
-- Migration V3: Social Features and Collaboration
-- ============================================================

CREATE TABLE comments (
    id              BIGSERIAL       PRIMARY KEY,
    repo_id         BIGINT          NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    author_id       BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_id       BIGINT          REFERENCES comments(id) ON DELETE CASCADE,
    content         TEXT            NOT NULL,
    is_suggestion   BOOLEAN         NOT NULL DEFAULT FALSE,
    file_path       VARCHAR(512),
    line_number     INTEGER,
    is_edited       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comments_repo ON comments(repo_id, created_at DESC);
CREATE INDEX idx_comments_parent ON comments(parent_id);

CREATE TABLE contribution_requests (
    id              BIGSERIAL       PRIMARY KEY,
    repo_id         BIGINT          NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    requester_id    BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    access_level    VARCHAR(20),
    message         TEXT,
    response_msg    TEXT,
    decided_at      TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_contrib_repo ON contribution_requests(repo_id, status);
CREATE INDEX idx_contrib_requester ON contribution_requests(requester_id);

CREATE TABLE repo_collaborators (
    id              BIGSERIAL       PRIMARY KEY,
    repo_id         BIGINT          NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_level    VARCHAR(20)     NOT NULL DEFAULT 'READ',
    granted_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_collaborator UNIQUE (repo_id, user_id)
);

CREATE INDEX idx_collab_repo ON repo_collaborators(repo_id);
CREATE INDEX idx_collab_user ON repo_collaborators(user_id);

CREATE TABLE copy_license_requests (
    id              BIGSERIAL       PRIMARY KEY,
    repo_id         BIGINT          NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    requester_id    BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    license_fee     DECIMAL(12, 2),
    message         TEXT,
    response_msg    TEXT,
    payment_status  VARCHAR(20)     DEFAULT 'NONE',
    decided_at      TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_copy_repo ON copy_license_requests(repo_id, status);
CREATE INDEX idx_copy_requester ON copy_license_requests(requester_id);

CREATE TABLE notifications (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type            VARCHAR(50)     NOT NULL,
    title           VARCHAR(255)    NOT NULL,
    message         TEXT,
    reference_type  VARCHAR(50),
    reference_id    BIGINT,
    is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notif_user ON notifications(user_id, is_read, created_at DESC);
