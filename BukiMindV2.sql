-- ============================================================================
-- BukiMind — Digital Reading & Knowledge Platform
-- PostgreSQL Schema — Production hardened
-- Version: 1.3   |   Requires PostgreSQL 15+
-- ============================================================================
-- Scope:
--   User, Book/Author/Publisher/Category, Library/Favorite, Review,
--   Document -> Chapter -> Page, Reading Progress,
--   Highlight/Note/Bookmark, Blog/Comment/Like, Subscription, Payment.
--   AI features live in a separate service (FastAPI / third-party).
--
-- Conventions:
--   - PK: UUID via gen_random_uuid() (built in since PG13, no extension).
--   - Timestamps: TIMESTAMPTZ (UTC). updated_at maintained by trigger.
--   - Enum-like fields: VARCHAR + CHECK (easy to evolve in migrations).
--   - Soft delete (deleted_at) on user-facing content. Unique constraints on
--     soft-deletable tables are PARTIAL (WHERE deleted_at IS NULL).
--   - Every FK has a supporting index (Postgres does NOT auto-index FKs).
--   - Child rows that must stay consistent with their parent's document/blog
--     use COMPOSITE FKs (e.g. an annotation's page must belong to the
--     annotation's document) — the DB enforces it, not just the app.
--   - Financial data is never cascade-deleted (ON DELETE RESTRICT).
--   - Money: BIGINT in the currency's MINOR unit (ISO 4217), column suffix
--     *_minor. The exponent lives in currencies.minor_unit:
--       USD -> 2 (999 = $9.99)      VND -> 0 (249000 = 249,000 d)
--     Gateway quirks (e.g. VNPay wants VND x 100) belong in the integration
--     layer, never in stored data.
--   - A plan is ONE row (e.g. PRO); its VND/USD prices live in plan_prices.
--
-- Migration notes:
--   - Run through Flyway/Liquibase; they already wrap in a transaction, so
--     drop BEGIN/COMMIT if your tool does that for you.
--   - Move the seed block (section 11) into its own versioned migration.
-- ============================================================================

BEGIN;

-- ============================================================================
-- 0. EXTENSIONS & COMMON FUNCTIONS
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS "citext";    -- case-insensitive email/username
CREATE EXTENSION IF NOT EXISTS "pg_trgm";   -- fuzzy search
CREATE EXTENSION IF NOT EXISTS "unaccent";  -- "nguyen" matches "Nguyễn"

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- unaccent() is only STABLE, so it cannot be used in an index directly.
-- This IMMUTABLE wrapper makes accent-insensitive trigram indexes possible.
-- Queries must use the same expression:
--   WHERE f_unaccent(title) ILIKE f_unaccent('%' || :q || '%')
CREATE OR REPLACE FUNCTION f_unaccent(text)
RETURNS text AS $$
    SELECT public.unaccent('public.unaccent', $1)
$$ LANGUAGE sql IMMUTABLE PARALLEL SAFE STRICT;

-- ============================================================================
-- 1. USER
-- ============================================================================
-- deleted_at is the single source of truth for deletion (no 'DELETED' status).
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_user_id    VARCHAR(255) NOT NULL UNIQUE,
    email               CITEXT NOT NULL UNIQUE,
    username            CITEXT NOT NULL UNIQUE,
    full_name           VARCHAR(255),
    avatar_url          TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'SUSPENDED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at          TIMESTAMPTZ
);
CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================================
-- 2. BOOK / AUTHOR / PUBLISHER / CATEGORY
-- ============================================================================
CREATE TABLE publishers (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE authors (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    bio         TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_authors_name_trgm ON authors USING gin (f_unaccent(name) gin_trgm_ops);

-- Hierarchical categories (Fiction > Sci-Fi). RESTRICT: you can't silently
-- orphan a whole subtree by deleting its parent.
CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id   UUID REFERENCES categories(id) ON DELETE RESTRICT,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE
                CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
    CHECK (parent_id IS NULL OR parent_id <> id)
);
CREATE INDEX idx_categories_parent_id ON categories(parent_id);

CREATE TABLE books (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(500) NOT NULL,
    description     TEXT,
    cover_url       TEXT,
    isbn            VARCHAR(20),
    language        VARCHAR(10) NOT NULL DEFAULT 'en',
    publisher_id    UUID REFERENCES publishers(id) ON DELETE SET NULL,
    published_date  DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                    CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ
);
CREATE TRIGGER trg_books_updated_at BEFORE UPDATE ON books
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE INDEX idx_books_publisher_id ON books(publisher_id);
-- ISBN stays unique among live books only, so a soft-deleted book doesn't
-- block re-adding the same ISBN.
CREATE UNIQUE INDEX uq_books_isbn ON books(isbn)
    WHERE isbn IS NOT NULL AND deleted_at IS NULL;
-- Public catalogue listing (newest first). Adjust the sort key to your UI.
CREATE INDEX idx_books_published ON books(created_at DESC)
    WHERE status = 'PUBLISHED' AND deleted_at IS NULL;
CREATE INDEX idx_books_title_trgm ON books USING gin (f_unaccent(title) gin_trgm_ops);

CREATE TABLE book_authors (
    book_id     UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    author_id   UUID NOT NULL REFERENCES authors(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);
CREATE INDEX idx_book_authors_author_id ON book_authors(author_id);

CREATE TABLE book_categories (
    book_id     UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, category_id)
);
CREATE INDEX idx_book_categories_category_id ON book_categories(category_id);

-- ============================================================================
-- 3. LIBRARY / FAVORITE
-- ============================================================================
CREATE TABLE library_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id     UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    status      VARCHAR(20) NOT NULL DEFAULT 'WANT_TO_READ'
                CHECK (status IN ('WANT_TO_READ', 'READING', 'COMPLETED', 'DROPPED')),
    added_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, book_id)
);
CREATE TRIGGER trg_library_items_updated_at BEFORE UPDATE ON library_items
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE INDEX idx_library_items_user_status ON library_items(user_id, status);
CREATE INDEX idx_library_items_book_id ON library_items(book_id);

CREATE TABLE favorites (
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id     UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, book_id)
);
CREATE INDEX idx_favorites_book_id ON favorites(book_id);

-- ============================================================================
-- 4. REVIEW
-- ============================================================================
CREATE TABLE reviews (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id     UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating      SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content     TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (book_id, user_id)   -- one review per user per book
);
CREATE TRIGGER trg_reviews_updated_at BEFORE UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE INDEX idx_reviews_user_id ON reviews(user_id);

-- ============================================================================
-- 5. DOCUMENT -> CHAPTER -> PAGE
-- ============================================================================
-- documents.book_id is RESTRICT: hard-deleting a book must not silently wipe
-- every reader's progress and highlights. Use soft delete (deleted_at).
-- storage_key = object key in S3/MinIO (NOT a full URL): the file is private,
-- the API issues signed URLs, and the CDN/domain can change without a data
-- migration.
CREATE TABLE documents (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id             UUID NOT NULL REFERENCES books(id) ON DELETE RESTRICT,
    storage_key         TEXT NOT NULL,
    file_type           VARCHAR(20) NOT NULL
                        CHECK (file_type IN ('PDF', 'EPUB', 'TXT')),
    file_size_bytes     BIGINT NOT NULL CHECK (file_size_bytes >= 0),
    processing_status   VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (processing_status IN
                            ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    processing_error    TEXT,
    total_pages         INT NOT NULL DEFAULT 0 CHECK (total_pages >= 0),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at          TIMESTAMPTZ
);
CREATE TRIGGER trg_documents_updated_at BEFORE UPDATE ON documents
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE INDEX idx_documents_book_id ON documents(book_id);
-- Worker queue: small index covering only unfinished jobs.
CREATE INDEX idx_documents_unprocessed ON documents(created_at)
    WHERE processing_status IN ('PENDING', 'PROCESSING');

CREATE TABLE chapters (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id     UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    chapter_number  INT NOT NULL CHECK (chapter_number > 0),
    title           VARCHAR(500),
    start_page      INT,
    end_page        INT,
    UNIQUE (document_id, chapter_number),
    UNIQUE (id, document_id),   -- target for composite FK from pages
    CHECK (start_page IS NULL OR end_page IS NULL OR end_page >= start_page)
);

CREATE TABLE pages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id     UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    chapter_id      UUID,
    page_number     INT NOT NULL CHECK (page_number > 0),
    content         TEXT NOT NULL,
    UNIQUE (document_id, page_number),
    UNIQUE (id, document_id),   -- target for composite FKs below
    -- A page's chapter must belong to the SAME document.
    FOREIGN KEY (chapter_id, document_id)
        REFERENCES chapters (id, document_id)
        ON DELETE SET NULL (chapter_id)
);
CREATE INDEX idx_pages_chapter_id ON pages(chapter_id);

-- ============================================================================
-- 6. READING PROGRESS
-- ============================================================================
CREATE TABLE reading_progress (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    document_id         UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    current_page_id     UUID,
    progress_percent    NUMERIC(5,2) NOT NULL DEFAULT 0
                        CHECK (progress_percent BETWEEN 0 AND 100),
    last_read_at        TIMESTAMPTZ,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, document_id),
    -- current page must belong to this document.
    FOREIGN KEY (current_page_id, document_id)
        REFERENCES pages (id, document_id)
        ON DELETE SET NULL (current_page_id)
);
CREATE TRIGGER trg_reading_progress_updated_at BEFORE UPDATE ON reading_progress
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE INDEX idx_reading_progress_document_id ON reading_progress(document_id);
CREATE INDEX idx_reading_progress_current_page_id ON reading_progress(current_page_id);
-- "Continue reading" shelf.
CREATE INDEX idx_reading_progress_recent ON reading_progress(user_id, last_read_at DESC);

-- ============================================================================
-- 7. ANNOTATION — highlight / note / bookmark
-- ============================================================================
-- page FK uses default NO ACTION on purpose: re-processing a document must
-- upsert pages by (document_id, page_number), never delete+insert, otherwise
-- users' highlights would be orphaned. Deleting a whole document still works
-- (annotations cascade from documents in the same statement).
CREATE TABLE annotations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    document_id     UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    page_id         UUID NOT NULL,
    type            VARCHAR(20) NOT NULL
                    CHECK (type IN ('HIGHLIGHT', 'NOTE', 'BOOKMARK')),
    selected_text   TEXT,               -- highlighted excerpt
    note_content    TEXT,               -- free-text note
    color           VARCHAR(20) NOT NULL DEFAULT 'YELLOW'
                    CHECK (color IN ('YELLOW', 'GREEN', 'BLUE', 'PINK', 'PURPLE')),
    position_start  INT CHECK (position_start >= 0),   -- char offset in page
    position_end    INT CHECK (position_end >= 0),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ,
    -- the page must belong to the same document.
    FOREIGN KEY (page_id, document_id) REFERENCES pages (id, document_id),
    CHECK (position_end IS NULL OR position_start IS NULL OR position_end >= position_start),
    -- required payload per type (a HIGHLIGHT may also carry a note)
    CHECK (type <> 'HIGHLIGHT'
           OR (selected_text IS NOT NULL
               AND position_start IS NOT NULL AND position_end IS NOT NULL)),
    CHECK (type <> 'NOTE' OR note_content IS NOT NULL)
);
CREATE TRIGGER trg_annotations_updated_at BEFORE UPDATE ON annotations
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE INDEX idx_annotations_user_document ON annotations(user_id, document_id)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_annotations_document_id ON annotations(document_id);
CREATE INDEX idx_annotations_page_id ON annotations(page_id);

-- ============================================================================
-- 8. BLOG / COMMENT / LIKE
-- ============================================================================
CREATE TABLE blogs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(500) NOT NULL,
    slug            VARCHAR(500) NOT NULL
                    CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
    content         TEXT NOT NULL,
    cover_url       TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                    CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    published_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ,
    CHECK (status <> 'PUBLISHED' OR published_at IS NOT NULL)
);
CREATE TRIGGER trg_blogs_updated_at BEFORE UPDATE ON blogs
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE UNIQUE INDEX uq_blogs_slug ON blogs(slug) WHERE deleted_at IS NULL;
CREATE INDEX idx_blogs_user_id ON blogs(user_id);
CREATE INDEX idx_blogs_status_published ON blogs(status, published_at DESC)
    WHERE deleted_at IS NULL;

CREATE TABLE blog_comments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    blog_id             UUID NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_comment_id   UUID,
    content             TEXT NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at          TIMESTAMPTZ,
    UNIQUE (id, blog_id),
    -- a reply must belong to the same blog as its parent.
    FOREIGN KEY (parent_comment_id, blog_id)
        REFERENCES blog_comments (id, blog_id) ON DELETE CASCADE
);
CREATE TRIGGER trg_blog_comments_updated_at BEFORE UPDATE ON blog_comments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE INDEX idx_blog_comments_blog_created ON blog_comments(blog_id, created_at)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_blog_comments_user_id ON blog_comments(user_id);
CREATE INDEX idx_blog_comments_parent_id ON blog_comments(parent_comment_id);

CREATE TABLE blog_likes (
    blog_id     UUID NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (blog_id, user_id)
);
CREATE INDEX idx_blog_likes_user_id ON blog_likes(user_id);

-- ============================================================================
-- 9. CURRENCY, PLAN & SUBSCRIPTION
-- ============================================================================
-- Supported currencies are DATA, not a CHECK: adding EUR later is an INSERT.
CREATE TABLE currencies (
    code        CHAR(3) PRIMARY KEY CHECK (code ~ '^[A-Z]{3}$'),
    name        VARCHAR(50) NOT NULL,
    minor_unit  SMALLINT NOT NULL CHECK (minor_unit BETWEEN 0 AND 4)
);

-- One row per sellable plan. NO price/currency here.
-- Plan code is a format check so adding 'PRO_YEARLY' is an INSERT.
CREATE TABLE subscription_plans (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                VARCHAR(50) NOT NULL UNIQUE
                        CHECK (code ~ '^[A-Z0-9_]+$'),
    name                VARCHAR(100) NOT NULL,
    billing_interval    VARCHAR(20) NOT NULL DEFAULT 'MONTHLY'
                        CHECK (billing_interval IN ('MONTHLY', 'YEARLY', 'LIFETIME')),
    features            JSONB NOT NULL DEFAULT '{}'::jsonb
);

-- Price of a plan in each currency: PRO has one VND price and one USD price,
-- but it is still the same plan.
CREATE TABLE plan_prices (
    plan_id         UUID NOT NULL REFERENCES subscription_plans(id) ON DELETE RESTRICT,
    currency        CHAR(3) NOT NULL REFERENCES currencies(code),
    amount_minor    BIGINT NOT NULL CHECK (amount_minor >= 0),
    PRIMARY KEY (plan_id, currency)
);
CREATE INDEX idx_plan_prices_currency ON plan_prices(currency);

-- The user picks ONE currency at purchase time. The composite FK guarantees
-- that currency is actually priced for that plan.
-- provider / provider_subscription_id map payment webhooks (e.g. Stripe
-- customer.subscription.*) back to the right row.
CREATE TABLE user_subscriptions (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id                  UUID NOT NULL,
    currency                 CHAR(3) NOT NULL,
    status                   VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                             CHECK (status IN ('ACTIVE', 'CANCELED', 'EXPIRED', 'PAST_DUE')),
    started_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    current_period_end       TIMESTAMPTZ,
    canceled_at              TIMESTAMPTZ,   -- may be set while still ACTIVE (cancel at period end)
    provider                 VARCHAR(50),
    provider_subscription_id VARCHAR(255),
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    FOREIGN KEY (plan_id, currency) REFERENCES plan_prices (plan_id, currency),
    UNIQUE (id, user_id),       -- targets for payments' composite FKs
    UNIQUE (id, currency),
    CHECK (current_period_end IS NULL OR current_period_end > started_at),
    CHECK ((provider IS NULL) = (provider_subscription_id IS NULL)),
    UNIQUE (provider, provider_subscription_id)
);
CREATE TRIGGER trg_user_subscriptions_updated_at BEFORE UPDATE ON user_subscriptions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE INDEX idx_user_subscriptions_plan_id ON user_subscriptions(plan_id, currency);
-- At most one live subscription per user, across ALL plans and currencies:
-- a user can never hold PRO-VND and PRO-USD at the same time. To change
-- currency, cancel the current subscription and start a new one.
CREATE UNIQUE INDEX uq_user_subscriptions_live_per_user
    ON user_subscriptions(user_id) WHERE status IN ('ACTIVE', 'PAST_DUE');

-- ============================================================================
-- 10. PAYMENT
-- ============================================================================
-- Financial records are append-mostly and must survive: RESTRICT on all FKs.
-- To remove a user, soft-delete / anonymize; never hard-delete payers.
-- When a payment belongs to a subscription, the composite FKs force its
-- user AND currency to match that subscription (no VND payment on a USD sub).
CREATE TABLE payments (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    subscription_id         UUID,
    amount_minor            BIGINT NOT NULL CHECK (amount_minor > 0),
    currency                CHAR(3) NOT NULL REFERENCES currencies(code),
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED')),
    provider                VARCHAR(50) NOT NULL,       -- 'STRIPE', 'VNPAY', 'MOMO', ...
    provider_payment_id     VARCHAR(255),               -- external transaction id
    paid_at                 TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    FOREIGN KEY (subscription_id, user_id)
        REFERENCES user_subscriptions (id, user_id) ON DELETE RESTRICT,
    FOREIGN KEY (subscription_id, currency)
        REFERENCES user_subscriptions (id, currency) ON DELETE RESTRICT,
    -- idempotent webhooks: the same provider event can't create two rows.
    UNIQUE (provider, provider_payment_id),
    CHECK (status NOT IN ('SUCCEEDED', 'REFUNDED') OR paid_at IS NOT NULL)
);
CREATE TRIGGER trg_payments_updated_at BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE INDEX idx_payments_user_id ON payments(user_id, created_at DESC);
CREATE INDEX idx_payments_subscription_id ON payments(subscription_id);
CREATE INDEX idx_payments_currency ON payments(currency);

-- ============================================================================
-- 11. SEED DATA (move to its own migration)
-- ============================================================================
INSERT INTO currencies (code, name, minor_unit) VALUES
    ('VND', 'Vietnamese dong', 0),
    ('USD', 'US dollar',       2)
ON CONFLICT (code) DO NOTHING;

INSERT INTO subscription_plans (code, name, billing_interval, features)
VALUES
    ('FREE', 'Free', 'LIFETIME',
     '{"read_books": true, "basic_library": true, "basic_highlight": true, "basic_notes": true}'::jsonb),
    ('PRO', 'Pro', 'MONTHLY',
     '{"unlimited_annotations": true, "ai_features": true, "advanced_stats": true}'::jsonb)
ON CONFLICT (code) DO NOTHING;

-- Placeholder prices — set your real ones. USD 9.99 = 999, VND 249,000 = 249000.
INSERT INTO plan_prices (plan_id, currency, amount_minor)
SELECT p.id, v.currency, v.amount_minor
FROM (VALUES
        ('FREE', 'VND', 0),
        ('FREE', 'USD', 0),
        ('PRO',  'VND', 249000),
        ('PRO',  'USD', 999)
     ) AS v(code, currency, amount_minor)
JOIN subscription_plans p ON p.code = v.code
ON CONFLICT (plan_id, currency) DO NOTHING;

COMMIT;