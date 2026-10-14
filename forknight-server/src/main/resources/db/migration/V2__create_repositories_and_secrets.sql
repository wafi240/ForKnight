-- ============================================================
-- Migration V2: Repositories and Secret Scan Results
-- ============================================================

CREATE TABLE repositories (
    id              BIGSERIAL       PRIMARY KEY,
    owner_id        BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    is_public       BOOLEAN         NOT NULL DEFAULT TRUE,
    license_type    VARCHAR(50)     DEFAULT 'NONE',
    default_branch  VARCHAR(100)    NOT NULL DEFAULT 'main',
    disk_path       VARCHAR(512)    NOT NULL,
    github_url      VARCHAR(512),
    stars_count     INTEGER         NOT NULL DEFAULT 0,
    forks_count     INTEGER         NOT NULL DEFAULT 0,
    is_for_sale     BOOLEAN         NOT NULL DEFAULT FALSE,
    sale_price      DECIMAL(12, 2),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_repo_owner_name UNIQUE (owner_id, name)
);

CREATE INDEX idx_repos_owner ON repositories(owner_id);
CREATE INDEX idx_repos_public ON repositories(is_public);
CREATE INDEX idx_repos_for_sale ON repositories(is_for_sale);

CREATE TABLE secret_scan_results (
    id              BIGSERIAL       PRIMARY KEY,
    repo_id         BIGINT          REFERENCES repositories(id) ON DELETE CASCADE,
    file_path       VARCHAR(512)    NOT NULL,
    line_number     INTEGER         NOT NULL,
    secret_type     VARCHAR(50)     NOT NULL,
    rule_name       VARCHAR(100)    NOT NULL,
    masked_snippet  VARCHAR(255),
    severity        VARCHAR(20)     NOT NULL DEFAULT 'HIGH',
    is_resolved     BOOLEAN         NOT NULL DEFAULT FALSE,
    detected_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_scan_repo ON secret_scan_results(repo_id);
CREATE INDEX idx_scan_severity ON secret_scan_results(severity);
