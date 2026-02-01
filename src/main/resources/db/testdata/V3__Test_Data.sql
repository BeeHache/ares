-- 1. Insert Accounts
INSERT INTO accounts (username, password, type, account_enabled_at)
VALUES ('user1@ares.com', 'user1_pass', 'USER', '2023-01-01 00:00:00')
ON CONFLICT (username, type) DO NOTHING;

INSERT INTO accounts (username, password, type, account_enabled_at)
VALUES ('user2@ares.com', 'user2_pass', 'USER', '2023-01-01 00:00:00')
ON CONFLICT (username, type) DO NOTHING;

-- 2. Insert Users (Linked to Account)
INSERT INTO users (email, account_id)
VALUES ('user1@ares.com', (SELECT id FROM accounts WHERE username = 'user1@ares.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, account_id)
VALUES ('user2@ares.com', (SELECT id FROM accounts WHERE username = 'user2@ares.com'))
ON CONFLICT (email) DO NOTHING;

-- 3. Insert Feeds
INSERT INTO feeds (url, is_podcast, last_modified)
VALUES ('https://feeds.bbci.co.uk/news/rss.xml', 'N', CURRENT_TIMESTAMP)
ON CONFLICT (url) DO NOTHING;

INSERT INTO feeds (url, is_podcast, last_modified)
VALUES ('https://feeds.simplecast.com/54nAGpIl', 'Y', CURRENT_TIMESTAMP)
ON CONFLICT (url) DO NOTHING;

-- 4. Link Users to Feeds
-- User 1 follows both
INSERT INTO subscriptions (user_id, feed_id)
VALUES (
    (SELECT id FROM users WHERE email = 'user1@ares.com'),
    (SELECT id FROM feeds WHERE url = 'https://feeds.bbci.co.uk/news/rss.xml')
)
ON CONFLICT (user_id, feed_id) DO NOTHING;

INSERT INTO subscriptions (user_id, feed_id)
VALUES (
    (SELECT id FROM users WHERE email = 'user1@ares.com'),
    (SELECT id FROM feeds WHERE url = 'https://feeds.simplecast.com/54nAGpIl')
)
ON CONFLICT (user_id, feed_id) DO NOTHING;

-- User 2 only follows BBC News
INSERT INTO subscriptions (user_id, feed_id)
VALUES (
    (SELECT id FROM users WHERE email = 'user2@ares.com'),
    (SELECT id FROM feeds WHERE url = 'https://feeds.bbci.co.uk/news/rss.xml')
)
ON CONFLICT (user_id, feed_id) DO NOTHING;
