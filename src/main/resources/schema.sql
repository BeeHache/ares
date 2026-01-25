CREATE EXTENSION IF NOT EXISTS "uuid-ossp";;

create table if not exists accounts (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(255) NOT NULL,
    password varchar(60) not null,
    type varchar(8) not null,
    account_expires_at timestamp with time zone,
    password_expires_at timestamp with time zone,
    account_locked_until timestamp with time zone,
    account_enabled_at timestamp with time zone,
    CONSTRAINT check_type CHECK (type IN ('ADMIN', 'USER')),
    CONSTRAINT username_type_unique UNIQUE (username, type)
);;

create table if not exists admins (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL unique,
    account_id BIGINT,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);;

create table if not exists users (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    email VARCHAR(255) NOT NULL unique,
    account_id BIGINT,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);;

create table if not exists roles (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL UNIQUE,
    parent_id BIGINT, -- This is the recursive link
    CONSTRAINT fk_role_parent FOREIGN KEY (parent_id) REFERENCES roles(id) ON DELETE CASCADE
);;

create table if not exists account_roles (
    account_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (account_id, role_id),
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);;

create table if not exists image(
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    content_type varchar(32),
    data bytea not null
);;

create table if not exists feeds (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    url varchar(512) not null unique,
    title varchar(255) not null,
    description varchar(2048),
    link varchar(512),
    is_podcast char(1) not null default 'N',
    last_modified timestamp with time zone,
    image_id UUID,
    CONSTRAINT check_is_podcast CHECK (is_podcast IN ('Y', 'N')),
    CONSTRAINT fk_feed_image FOREIGN KEY (image_id) REFERENCES image(id) ON DELETE CASCADE
);;

create index if not exists ix_feeds_last_mod on feeds (last_modified);;

create table if not exists feed_item (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    feed_id UUID NOT NULL,
    title VARCHAR(255),
    description VARCHAR(4096),
    link VARCHAR(512) UNIQUE,
    date timestamp with time zone,
    image_id UUID,
    CONSTRAINT fk_feed FOREIGN KEY (feed_id) REFERENCES feeds(id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_item_image FOREIGN KEY (image_id) REFERENCES image(id) ON DELETE CASCADE
);;

create table if not exists enclosures
(
    id           UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    feed_item_id UUID NOT NULL,
    url          varchar(512) not null unique,
    length       BIGINT,
    type         varchar(32),
    CONSTRAINT fk_feed_item FOREIGN KEY (feed_item_id) REFERENCES feed_item (id) ON DELETE CASCADE
);;

create table if not exists user_feeds (
    user_id BIGINT NOT NULL,
    feed_id UUID NOT NULL,
    PRIMARY KEY (user_id, feed_id), -- Prevents a user from subscribing to the same feed twice
    CONSTRAINT fk_user_link FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_link FOREIGN KEY (feed_id) REFERENCES feeds(id) ON DELETE CASCADE
);;

create table if not exists user_feed_item (
    user_id BIGINT NOT NULL,
    feed_item_id UUID NOT NULL,
    read_at timestamp with time zone,
    archive_at timestamp with time zone,
    PRIMARY KEY (user_id, feed_item_id), -- Prevents a user from reading the same feed item twice
    CONSTRAINT fk_user_link FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_item_link FOREIGN KEY (feed_item_id) REFERENCES feed_item(id) ON DELETE CASCADE
);;

CREATE OR REPLACE FUNCTION update_last_modified_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.last_modified = NOW();
   RETURN NEW;
END;
$$ language 'plpgsql';;

DROP TRIGGER IF EXISTS update_feeds_last_modified ON feeds;;

CREATE TRIGGER update_feeds_last_modified
BEFORE UPDATE ON feeds
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_column();;
