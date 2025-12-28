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
    description varchar(1024),
    link varchar(255) not null,
    is_podcast char(1) not null default 'N',
    last_modified timestamp
);

create index ix_feeds_last_mod on feeds (last_modified);

create table feed_item (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    feed_id BIGINT NOT NULL,
    title VARCHAR(255),
    description VARCHAR(2048),
    link VARCHAR(512),
    image VARCHAR(512),
    date VARCHAR(255),
    CONSTRAINT fk_feed FOREIGN KEY (feed_id) REFERENCES feeds(id) ON DELETE CASCADE
    );

create table user_feeds (
    user_id BIGINT NOT NULL,
    feed_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, feed_id), -- Prevents a user from subscribing to the same feed twice
    CONSTRAINT fk_user_link FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_link FOREIGN KEY (feed_id) REFERENCES feeds(id) ON DELETE CASCADE
);