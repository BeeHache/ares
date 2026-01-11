-- 1. Insert Admins
-- Passwords should be hashed in a real app, but using plain text for testing
INSERT INTO admins (name, email, password)
VALUES ('Admin User', 'admin@ares.com', 'admin_pass')
ON CONFLICT (email) DO NOTHING;


-- 2. Insert Recursive Roles (Adjacency List)
-- First, insert top-level roles
INSERT INTO roles (name, parent_id) VALUES ('SUPER_ADMIN', NULL) ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, parent_id) VALUES ('EDITOR', NULL) ON CONFLICT (name) DO NOTHING;

-- Next, insert sub-roles linked to parents
INSERT INTO roles (name, parent_id) VALUES ('USER_MANAGER', 1) ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, parent_id) VALUES ('FEED_MODERATOR', 2) ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, parent_id) VALUES ('JUNIOR_EDITOR', 4) ON CONFLICT (name) DO NOTHING;

-- 3. Assign Roles to Admins
-- Assign SUPER_ADMIN (ID 1) to admin@ares.com (ID 1)
INSERT INTO admins_roles (admin_id, role_id) VALUES (1, 1) ON CONFLICT (admin_id, role_id) DO NOTHING;


-- 4. Insert Users
INSERT INTO users (email, password) VALUES ('user1@ares.com', 'user1_pass') ON CONFLICT (email) DO NOTHING;
INSERT INTO users (email, password) VALUES ('user2@ares.com', 'user2_pass') ON CONFLICT (email) DO NOTHING;

-- 5. Insert Feeds (Global Registry)
INSERT INTO feeds (title, description, link, is_podcast, last_modified)
VALUES ('BBC News', 'World News from London', 'https://feeds.bbci.co.uk/news/rss.xml', 'N', CURRENT_TIMESTAMP)
ON CONFLICT (link) DO NOTHING;

INSERT INTO feeds (title, description, link, is_podcast, last_modified)
VALUES ('The Daily', 'A New York Times Podcast', 'https://feeds.simplecast.com/54nAGpIl', 'Y', CURRENT_TIMESTAMP)
ON CONFLICT (link) DO NOTHING;


-- 4. Link Users to Feeds (Many-to-Many Join Table)
-- User 1 follows both
INSERT INTO user_feeds (user_id, feed_id) VALUES (1, 1) ON CONFLICT (user_id, feed_id) DO NOTHING;
INSERT INTO user_feeds (user_id, feed_id) VALUES (1, 2) ON CONFLICT (user_id, feed_id) DO NOTHING;
-- User 2 only follows BBC News
INSERT INTO user_feeds (user_id, feed_id) VALUES (2, 1) ON CONFLICT (user_id, feed_id) DO NOTHING;

-- 5. Insert Feed Items (Linked to Feeds)
-- Items for BBC News (Feed ID 1)
INSERT INTO feed_item (feed_id, title, description, link, date)
VALUES (1, 'Breaking News', 'Something big happened today.', 'https://bbc.com/news/1', '2025-12-21 12:00:00')
ON CONFLICT (link) DO NOTHING;

-- Items for The Daily (Feed ID 2)
INSERT INTO feed_item (feed_id, title, description, link, date)
VALUES (2, 'The Sunday Read', 'A deep dive into politics.', 'https://nytimes.com/podcast/1', '2025-12-21 08:00:00')
ON CONFLICT (link) DO NOTHING;
