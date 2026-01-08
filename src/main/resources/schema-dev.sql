create table admins (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL unique,
    password varchar(60) not null
);

create table role (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT, -- This is the recursive link
    CONSTRAINT fk_role_parent FOREIGN KEY (parent_id) REFERENCES role(id) ON DELETE CASCADE
);

create table admin_roles (
    admin_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (admin_id, role_id),
    FOREIGN KEY (admin_id) REFERENCES admins(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

create table users (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    email VARCHAR(255) NOT NULL unique,
    password varchar(60) not null
);

create table feeds (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    title varchar(255) not null,
    description varchar(2048),
    link varchar(512) not null unique,
    is_podcast char(1) not null default 'N',
    last_modified timestamp,
    image_id BIGINT,
    CONSTRAINT check_is_podcast CHECK (is_podcast IN ('Y', 'N')),
    CONSTRAINT fk_feed_image FOREIGN KEY (image_id) REFERENCES image(id) ON DELETE CASCADE
);

create index ix_feeds_last_mod on feeds (last_modified);

create table feed_item (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    feed_id BIGINT NOT NULL,
    title VARCHAR(255),
    description VARCHAR(2048),
    link VARCHAR(512),
    date timestamp,
    image_id BIGINT,
    CONSTRAINT fk_feed FOREIGN KEY (feed_id) REFERENCES feeds(id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_item_image FOREIGN KEY (image_id) REFERENCES image(id) ON DELETE CASCADE
);

create table user_feeds (
    user_id BIGINT NOT NULL,
    feed_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, feed_id), -- Prevents a user from subscribing to the same feed twice
    CONSTRAINT fk_user_link FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_link FOREIGN KEY (feed_id) REFERENCES feeds(id) ON DELETE CASCADE
);

create table refresh_token (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date timestamp NOT NULL,
    CONSTRAINT fk_user_refresh FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

create table image(
    id BIGINT NOT NULL PRIMARY KEY  GENERATED ALWAYS AS IDENTITY,
    content_type varchar(32),
    data bytea not null
);

