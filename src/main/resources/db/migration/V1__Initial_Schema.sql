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
    url varchar(512) not null unique,
    podcast varchar(1) not null default 'N',
    last_modified timestamp with time zone,
    dto JSONB,
    CHECK (podcast IN ('Y', 'N'))
);

create index if not exists ix_feeds_last_mod on feeds (last_modified);
create index if not exists ix_feeds_dto_title on feeds ((dto ->>'title'));
create index if not exists ix_feeds_dto_description on feeds ((dto ->> 'description'));
create index if not exists ix_feeds_dto_podcast on feeds ((dto ->> 'podcast'));
create index if not exists ix_feeds_dto on feeds USING GIN (dto);

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
BEFORE UPDATE ON feeds
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_column();
