DELETE FROM operation;

ALTER TABLE operation ADD COLUMN user_id TEXT NOT NULL;

CREATE INDEX operation_user_account_idx ON operation (user_id, account_id);
