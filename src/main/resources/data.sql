-- 1. Insert Accounts
-- Passwords should be hashed in a real app
INSERT INTO accounts (username, password, type)
VALUES ('admin@ares.com', 'admin_pass', 'ADMIN')
ON CONFLICT (username, type) DO NOTHING;

INSERT INTO accounts (username, password, type)
VALUES ('user1@ares.com', 'user1_pass', 'USER')
ON CONFLICT (username, type) DO NOTHING;

INSERT INTO accounts (username, password, type)
VALUES ('user2@ares.com', 'user2_pass', 'USER')
ON CONFLICT (username, type) DO NOTHING;


-- 2. Insert Admins (Linked to Account)
-- Assuming ID 1 is the admin account
INSERT INTO admins (name, email, account_id)
VALUES ('Admin User', 'admin@ares.com', 1)
ON CONFLICT (email) DO NOTHING;


-- 3. Insert Users (Linked to Account)
-- Assuming ID 2 and 3 are user accounts
INSERT INTO users (email, account_id)
VALUES ('user1@ares.com', 2)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, account_id)
VALUES ('user2@ares.com', 3)
ON CONFLICT (email) DO NOTHING;


-- 4. Insert Recursive Roles
INSERT INTO roles (name, parent_id) VALUES ('SUPER_ADMIN', NULL) ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, parent_id) VALUES ('EDITOR', NULL) ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, parent_id) VALUES ('USER_MANAGER', 1) ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, parent_id) VALUES ('FEED_MODERATOR', 2) ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, parent_id) VALUES ('JUNIOR_EDITOR', 4) ON CONFLICT (name) DO NOTHING;


-- 5. Assign Roles to Accounts
-- Assign SUPER_ADMIN (ID 1) to admin account (ID 1)
INSERT INTO account_roles (account_id, role_id) VALUES (1, 1) ON CONFLICT (account_id, role_id) DO NOTHING;


-- 6. Insert Feeds
INSERT INTO feeds (title, description, link, is_podcast, last_modified)
VALUES ('BBC News', 'World News from London', 'https://feeds.bbci.co.uk/news/rss.xml', 'N', CURRENT_TIMESTAMP)
ON CONFLICT (link) DO NOTHING;

INSERT INTO feeds (title, description, link, is_podcast, last_modified)
VALUES ('The Daily', 'A New York Times Podcast', 'https://feeds.simplecast.com/54nAGpIl', 'Y', CURRENT_TIMESTAMP)
ON CONFLICT (link) DO NOTHING;


-- 7. Link Users to Feeds
-- User 1 (ID 1) follows both
INSERT INTO user_feeds (user_id, feed_id) VALUES (1, 1) ON CONFLICT (user_id, feed_id) DO NOTHING;
INSERT INTO user_feeds (user_id, feed_id) VALUES (1, 2) ON CONFLICT (user_id, feed_id) DO NOTHING;
-- User 2 (ID 2) only follows BBC News
INSERT INTO user_feeds (user_id, feed_id) VALUES (2, 1) ON CONFLICT (user_id, feed_id) DO NOTHING;


-- 8. Insert Feed Items
INSERT INTO feed_item (feed_id, title, description, link, date)
VALUES (1, 'Breaking News', 'Something big happened today.', 'https://bbc.com/news/1', '2025-12-21 12:00:00')
ON CONFLICT (link) DO NOTHING;

INSERT INTO feed_item (feed_id, title, description, link, date)
VALUES (2, 'The Sunday Read', 'A deep dive into politics.', 'https://nytimes.com/podcast/1', '2025-12-21 08:00:00')
ON CONFLICT (link) DO NOTHING;
