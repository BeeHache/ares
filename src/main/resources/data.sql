INSERT INTO roles (name, parent_id) VALUES ('SUPER_ADMIN', NULL) ON CONFLICT (name) DO NOTHING;;
INSERT INTO roles (name, parent_id) VALUES ('EDITOR', NULL) ON CONFLICT (name) DO NOTHING;;
INSERT INTO roles (name, parent_id) VALUES ('USER_MANAGER', 1) ON CONFLICT (name) DO NOTHING;;
INSERT INTO roles (name, parent_id) VALUES ('FEED_MODERATOR', 2) ON CONFLICT (name) DO NOTHING;;
INSERT INTO roles (name, parent_id) VALUES ('JUNIOR_EDITOR', 4) ON CONFLICT (name) DO NOTHING;;
