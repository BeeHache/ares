create table if not exists "users" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL unique,
    password varchar(60) not null
);

CREATE TABLE if not exists role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    parent_id INT, -- This is the recursive link
    CONSTRAINT fk_role_parent
    FOREIGN KEY (parent_id)
    REFERENCES role(id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES "users"(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

create table if not exists "feeds" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title varchar(255) not null,
    description varchar(1024),
    link varchar(255) not null,
    is_podcast char(1) not null default 'N',
    last_modified timestamp
);

CREATE TABLE IF NOT EXISTS "feed_item" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    feed_id INT NOT NULL,
    title VARCHAR(255),
    description VARCHAR(2048),
    link VARCHAR(512),
    image VARCHAR(512),
    date VARCHAR(255),
    CONSTRAINT fk_feed FOREIGN KEY (feed_id) REFERENCES "feeds"(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS user_feeds (
    user_id INT NOT NULL,
    feed_id INT NOT NULL,
    PRIMARY KEY (user_id, feed_id), -- Prevents a user from subscribing to the same feed twice
    CONSTRAINT fk_user_link FOREIGN KEY (user_id) REFERENCES "users"(id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_link FOREIGN KEY (feed_id) REFERENCES "feeds"(id) ON DELETE CASCADE
);