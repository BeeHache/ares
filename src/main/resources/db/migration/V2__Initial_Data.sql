-- 1. Insert Accounts
-- Passwords should be hashed in a real app
INSERT INTO accounts (username, password, type)
VALUES ('${ADMIN_EMAIL}', '${ADMIN_PASSWORD}', 'ADMIN')
ON CONFLICT (username, type) DO NOTHING;

-- 2. Insert Admins (Linked to Account)
-- Assuming ID 1 is the admin account
INSERT INTO admins (name, email, account_id)
VALUES ('Admin User', '${ADMIN_EMAIL}', (SELECT id FROM accounts WHERE username = '${ADMIN_EMAIL}'))
ON CONFLICT (email) DO NOTHING;

-- 3. Insert Recursive Roles
INSERT INTO roles (name, parent_id) VALUES ('SUPER_ADMIN', NULL) ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, parent_id) VALUES ('EDITOR', NULL) ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, parent_id) VALUES ('USER_MANAGER', 1) ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, parent_id) VALUES ('FEED_MODERATOR', 2) ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, parent_id) VALUES ('JUNIOR_EDITOR', 4) ON CONFLICT (name) DO NOTHING;

-- 4. Assign Roles to Accounts
-- Assign SUPER_ADMIN (ID 1) to admin account (ID 1)
INSERT INTO account_roles (account_id, role_id)
VALUES ((SELECT id FROM accounts WHERE username = '${ADMIN_EMAIL}'), (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'))
ON CONFLICT (account_id, role_id) DO NOTHING;
