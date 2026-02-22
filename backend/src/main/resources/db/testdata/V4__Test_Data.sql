-- 1. Insert Accounts
-- user1_pass
INSERT INTO accounts (username, password, type, account_enabled_at)
VALUES ('user1@ares.com', '$2y$12$KBrYuP1ZGySR8Ufqwe4YE.yb8Vf5N/0eFQS6CgJU4HzGo2TYF5206', 'USER', '2023-01-01 00:00:00')
ON CONFLICT (username, type) DO NOTHING;

--user2_pass
INSERT INTO accounts (username, password, type, account_enabled_at)
VALUES ('user2@ares.com', '$2y$12$LfkT8UB5wa/HGjoiRdXeUuvTtOheTMx6JMG4agzvB3tjtSxgNW3A', 'USER', '2023-01-01 00:00:00')
ON CONFLICT (username, type) DO NOTHING;

-- 2. Insert Users (Linked to Account)
INSERT INTO users (email, account_id)
VALUES ('user1@ares.com', (SELECT id FROM accounts WHERE username = 'user1@ares.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, account_id)
VALUES ('user2@ares.com', (SELECT id FROM accounts WHERE username = 'user2@ares.com'))
ON CONFLICT (email) DO NOTHING;

-- 3. Insert Feeds
INSERT INTO feeds (url, podcast, last_modified)
VALUES ('https://feeds.bbci.co.uk/news/rss.xml', 'N', CURRENT_TIMESTAMP - INTERVAL '24 hours')
ON CONFLICT (url) DO NOTHING;

INSERT INTO feeds (url, podcast, last_modified)
VALUES ('https://feeds.npr.org/500005/podcast.xml', 'Y', CURRENT_TIMESTAMP - INTERVAL '24 hours')
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
    (SELECT id FROM feeds WHERE url = 'https://feeds.npr.org/500005/podcast.xml')
)
ON CONFLICT (user_id, feed_id) DO NOTHING;

-- User 2 only follows BBC News
INSERT INTO subscriptions (user_id, feed_id)
VALUES (
    (SELECT id FROM users WHERE email = 'user2@ares.com'),
    (SELECT id FROM feeds WHERE url = 'https://feeds.bbci.co.uk/news/rss.xml')
)
ON CONFLICT (user_id, feed_id) DO NOTHING;
