CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table if not exists accounts (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(255) NOT NULL,
    password varchar(60) not null,
    type varchar(8) not null,
    account_expires_at timestamp with time zone,
    password_expires_at timestamp with time zone,
    account_locked_until timestamp with time zone,
    account_enabled_at timestamp with time zone,
    CHECK (type IN ('ADMIN', 'USER')),
    UNIQUE (username, type)
);

create table if not exists admins (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL unique,
    account_id BIGINT,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

create table if not exists users (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    email VARCHAR(255) NOT NULL unique,
    account_id BIGINT,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

create table if not exists roles (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL UNIQUE,
    parent_id BIGINT, -- This is the recursive link
    FOREIGN KEY (parent_id) REFERENCES roles(id) ON DELETE CASCADE
);

create table if not exists account_roles (
    account_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (account_id, role_id),
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

create table if not exists feeds (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    title varchar(255),
    description text,
    url varchar(512) not null unique,
    link varchar(512),
    image_url varchar(512),
    podcast varchar(1) not null default 'N',
    pubdate timestamp with time zone,
    last_modified timestamp with time zone,
    search_vector tsvector, -- Added for full-text search
    CHECK (podcast IN ('Y', 'N'))
);

create index if not exists ix_feeds_podcast on feeds (podcast);
create index if not exists ix_feeds_pudate on feeds (pubdate);
create index if not exists ix_feeds_last_mod on feeds (last_modified);
create index if not exists ix_feeds_search_vector on feeds USING GIN (search_vector);

create table if not exists feed_items(
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    guid varchar(255),
    feed_id UUID NOT NULL,
    title varchar(255) NOT NULL,
    description text,
    link varchar(512),
    date timestamp with time zone,
    last_modified timestamp with time zone,
    search_vector tsvector, -- Added for full-text search
    FOREIGN KEY (feed_id) references feeds(id) ON DELETE CASCADE,
    UNIQUE (feed_id, title)
);

create index if not exists ix_feed_items_guid on feed_items (guid);
create index if not exists ix_feed_items_title on feed_items (title);
create index if not exists ix_feed_items_link on feed_items (link);
create index if not exists ix_feed_items_date on feed_items (date);
create index if not exists ix_feed_items_search_vector on feed_items USING GIN (search_vector);


create table if not exists enclosures(
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    feed_item_id UUID,
    url varchar(512),
    length BIGINT,
    type varchar(64),
    FOREIGN KEY (feed_item_id) references feed_items(id) ON DELETE CASCADE
);


create table if not exists feed_image (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    feed_id UUID,
    image_url varchar(512),
    content_type varchar(32),
    data BYTEA,
    FOREIGN KEY (feed_id) references feeds(id) ON DELETE CASCADE
);


create table if not exists subscriptions (
    user_id BIGINT NOT NULL,
    feed_id UUID NOT NULL,
    PRIMARY KEY (user_id, feed_id), -- Prevents a user from subscribing to the same feed twice
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (feed_id) REFERENCES feeds(id) ON DELETE CASCADE
);


CREATE OR REPLACE FUNCTION update_last_modified_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.last_modified = NOW();
   RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_feeds_last_modified ON feeds;
CREATE TRIGGER update_feeds_last_modified
BEFORE INSERT OR UPDATE ON feeds
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_column();

DROP TRIGGER IF EXISTS update_feed_items_last_modified ON feed_items;
CREATE TRIGGER update_feed_items_last_modified
    BEFORE INSERT OR UPDATE ON feed_items
    FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_column();

CREATE OR REPLACE FUNCTION update_search_vector_column() RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector :=
            setweight(to_tsvector('english', coalesce(NEW.title, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(NEW.description, '')), 'B');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tsvectorupdate_feeds ON feeds;
CREATE TRIGGER tsvectorupdate_feeds BEFORE INSERT OR UPDATE
    ON feeds FOR EACH ROW EXECUTE FUNCTION update_search_vector_column();

DROP TRIGGER IF EXISTS tsvectorupdate_feed_items ON feed_items;
CREATE TRIGGER tsvectorupdate_feed_items BEFORE INSERT OR UPDATE
    ON feed_items FOR EACH ROW EXECUTE FUNCTION update_search_vector_column();
