ALTER TABLE accounts ADD canceled_at timestamp with time zone;
create index if not exists ix_accounts_canceled_at on accounts (canceled_at);

create table if not exists canceled_users (
    user_id BIGINT NOT NULL PRIMARY KEY,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
