-- 1. Insert Accounts
-- Passwords should be hashed in a real app
INSERT INTO accounts (username, password, type)
VALUES ('${ADMIN_EMAIL}', '${ADMIN_PASSWORD}', 'ADMIN')
ON CONFLICT (username, type) DO NOTHING;

-- 2. Insert Admins (Linked to Account)
INSERT INTO admins (name, email, account_id)
VALUES ('Admin User', '${ADMIN_EMAIL}', (SELECT id FROM accounts WHERE username = '${ADMIN_EMAIL}'))
ON CONFLICT (email) DO NOTHING;

-- 3. Insert Recursive Roles
-- aAdmin roles
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


