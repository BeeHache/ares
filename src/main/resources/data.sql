-- 1. Insert Users
-- Passwords should be hashed in a real app, but using plain text for testing
-- INSERT INTO USER (email, password) VALUES ('admin@ares.com', 'admin_pass');
INSERT INTO users (email, password)
VALUES ('admin@ares.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u');

INSERT INTO users (email, password)
VALUES ('ben@ares.com', '$2a$10$76at/qFmJq7Z1W9jJk.u6uT6vBvF6X4/9XpX9XpX9XpX9XpX9XpX9');

-- 2. Insert Recursive Roles (Adjacency List)
-- First, insert top-level roles
INSERT INTO role (name, parent_id) VALUES ('SUPER_ADMIN', NULL); -- ID 1
INSERT INTO role (name, parent_id) VALUES ('EDITOR', NULL);      -- ID 2

-- Next, insert sub-roles linked to parents
INSERT INTO role (name, parent_id) VALUES ('USER_MANAGER', 1);   -- ID 3 (Child of SUPER_ADMIN)
INSERT INTO role (name, parent_id) VALUES ('FEED_MODERATOR', 2); -- ID 4 (Child of EDITOR)
INSERT INTO role (name, parent_id) VALUES ('JUNIOR_EDITOR', 4);  -- ID 5 (Child of FEED_MODERATOR)

-- 3. Insert Feeds (Global Registry)
INSERT INTO feeds (title, description, link, is_podcast, last_modified)
VALUES ('BBC News', 'World News from London', 'https://feeds.bbci.co.uk/news/rss.xml', 'N', CURRENT_TIMESTAMP);

INSERT INTO feeds (title, description, link, is_podcast, last_modified)
VALUES ('The Daily', 'A New York Times Podcast', 'https://feeds.simplecast.com/54nAGpIl', 'Y', CURRENT_TIMESTAMP);

-- 4. Link Users to Feeds (Many-to-Many Join Table)
-- Admin follows both
INSERT INTO user_feeds (user_id, feed_id) VALUES (1, 1);
INSERT INTO user_feeds (user_id, feed_id) VALUES (1, 2);
-- Ben only follows BBC News
INSERT INTO user_feeds (user_id, feed_id) VALUES (2, 1);

-- 5. Insert Feed Items (Linked to Feeds)
-- Items for BBC News (Feed ID 1)
INSERT INTO feed_item (feed_id, title, description, link, date)
VALUES (1, 'Breaking News', 'Something big happened today.', 'https://bbc.com/news/1', '2025-12-21T12:00:00Z');

-- Items for The Daily (Feed ID 2)
INSERT INTO feed_item (feed_id, title, description, link, date)
VALUES (2, 'The Sunday Read', 'A deep dive into politics.', 'https://nytimes.com/podcast/1', '2025-12-21T08:00:00Z');

-- 6. Assign Roles to Users
-- Assign SUPER_ADMIN (ID 1) to admin@ares.com (ID 1)
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
