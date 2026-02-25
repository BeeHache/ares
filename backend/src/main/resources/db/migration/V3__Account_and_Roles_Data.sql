-- 1. Insert Accounts
-- Passwords should be hashed in a real app
INSERT INTO accounts (username, password, type)
VALUES ('${ADMIN_EMAIL}', '${ADMIN_PASSWORD}', 'ADMIN')
ON CONFLICT (username, type) DO NOTHING;

INSERT INTO accounts (username, password, type, account_enabled_at)
VALUES ('${TEST_USER1_EMAIL}', '${TEST_USER1_PW}', 'USER', '2023-01-01 00:00:00')
ON CONFLICT (username, type) DO NOTHING;

INSERT INTO accounts (username, password, type, account_enabled_at)
VALUES ('${TEST_USER2_EMAIL}', '${TEST_USER2_PW}', 'USER', '2023-01-01 00:00:00')
ON CONFLICT (username, type) DO NOTHING;

INSERT INTO accounts (username, password, type, account_enabled_at)
VALUES ('${TEST_USER3_EMAIL}', '${TEST_USER3_PW}', 'USER', '2023-01-01 00:00:00')
ON CONFLICT (username, type) DO NOTHING;


-- 2. Insert Admins (Linked to Account)
INSERT INTO admins (name, email, account_id)
VALUES ('Admin User', '${ADMIN_EMAIL}', (SELECT id FROM accounts WHERE username = '${ADMIN_EMAIL}'))
ON CONFLICT (email) DO NOTHING;

-- 3. Insert Test Users (linked to Account)
INSERT INTO users (email, account_id)
VALUES ('${TEST_USER1_EMAIL}', (SELECT id FROM accounts WHERE username = '${TEST_USER1_EMAIL}'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, account_id)
VALUES ('${TEST_USER2_EMAIL}', (SELECT id FROM accounts WHERE username = '${TEST_USER2_EMAIL}'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, account_id)
VALUES ('${TEST_USER3_EMAIL}', (SELECT id FROM accounts WHERE username = '${TEST_USER3_EMAIL}'))
ON CONFLICT (email) DO NOTHING;


-- 4. Insert Recursive Roles
INSERT INTO roles (name) VALUES ('SUPER_ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('EDITOR') ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, parent_id)
VALUES ('USER_MANAGER',
        (select id from roles where name='EDITOR'))
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, parent_id)
VALUES ('FEED_MODERATOR',
        (select id from roles where name='EDITOR'))
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, parent_id)
VALUES ('JUNIOR_EDITOR',
        (select id from roles where name='FEED_MODERATOR'))
ON CONFLICT (name) DO NOTHING;


-- User Roles
INSERT INTO roles (name) VALUES ('TEST_USER') ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, parent_id)
VALUES ('USER',
        (select id from roles where name='TEST_USER'))
ON CONFLICT (name) DO NOTHING;


-- 5. Assign Roles to Accounts
-- Assign SUPER_ADMIN (ID 1) to admin account (ID 1)
INSERT INTO account_roles (account_id, role_id)
VALUES ((SELECT id FROM accounts WHERE username = '${ADMIN_EMAIL}'),
        (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'))
ON CONFLICT (account_id, role_id) DO NOTHING;


--6. Assign TEST_USER role to test user accounts
INSERT INTO account_roles (account_id, role_id)
VALUES ((SELECT id FROM accounts WHERE username = '${TEST_USER1_EMAIL}'),
        (SELECT id FROM roles WHERE name = 'TEST_USER'))
ON CONFLICT (account_id, role_id) DO NOTHING;

INSERT INTO account_roles (account_id, role_id)
VALUES ((SELECT id FROM accounts WHERE username = '${TEST_USER2_EMAIL}'),
        (SELECT id FROM roles WHERE name = 'TEST_USER'))
ON CONFLICT (account_id, role_id) DO NOTHING;

INSERT INTO account_roles (account_id, role_id)
VALUES ((SELECT id FROM accounts WHERE username = '${TEST_USER3_EMAIL}'),
        (SELECT id FROM roles WHERE name = 'TEST_USER'))
ON CONFLICT (account_id, role_id) DO NOTHING;

