-- ============================================================
-- Migration V4: Code Marketplace and Orders
-- ============================================================

CREATE TABLE snippets (
    id              BIGSERIAL       PRIMARY KEY,
    author_id       BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(150)    NOT NULL,
    description     TEXT,
    code_content    TEXT            NOT NULL,
    language        VARCHAR(50)     NOT NULL DEFAULT 'java',
    price           DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    sales_count     INTEGER         NOT NULL DEFAULT 0,
    rating_avg      DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    rating_count    INTEGER         NOT NULL DEFAULT 0,
    tags            VARCHAR(255),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_snippets_author ON snippets(author_id);
CREATE INDEX idx_snippets_lang ON snippets(language);
CREATE INDEX idx_snippets_price ON snippets(price);

CREATE TABLE orders (
    id              BIGSERIAL       PRIMARY KEY,
    buyer_id        BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    seller_id       BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_type       VARCHAR(30)     NOT NULL,
    item_id         BIGINT          NOT NULL,
    amount          DECIMAL(12, 2)  NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'COMPLETED',
    payment_method  VARCHAR(50)     DEFAULT 'WALLET',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_buyer ON orders(buyer_id);
CREATE INDEX idx_orders_seller ON orders(seller_id);
CREATE INDEX idx_orders_item ON orders(item_type, item_id);

CREATE TABLE snippet_ratings (
    id              BIGSERIAL       PRIMARY KEY,
    snippet_id      BIGINT          NOT NULL REFERENCES snippets(id) ON DELETE CASCADE,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    stars           INTEGER         NOT NULL CHECK (stars >= 1 AND stars <= 5),
    review          TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_snippet_user_rating UNIQUE (snippet_id, user_id)
);

CREATE INDEX idx_ratings_snippet ON snippet_ratings(snippet_id);
