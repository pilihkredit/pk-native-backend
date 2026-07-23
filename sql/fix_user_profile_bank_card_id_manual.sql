-- Emergency one-shot for environments stuck without `id`.
-- Use the SAME database the app connects to (check spring datasource).
-- Run these statements one by one and confirm each succeeds.

-- A) Check current state
SHOW CREATE TABLE user_profile_bank_card\G
SELECT DATABASE();

-- B) If PRIMARY KEY is still (profile_id), drop it
--    Skip this statement if SHOW CREATE TABLE already has no PRIMARY KEY.
ALTER TABLE user_profile_bank_card DROP PRIMARY KEY;

-- C) Add id as PK (fills existing rows automatically)
--    Skip if column `id` already exists.
--    Clause order matters on MySQL 5.7: ... AUTO_INCREMENT PRIMARY KEY COMMENT ... FIRST
ALTER TABLE user_profile_bank_card
  ADD COLUMN id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key' FIRST;

-- D) Index profile_id if missing
CREATE INDEX idx_user_profile_bank_card_profile_id ON user_profile_bank_card (profile_id);

-- E) Soft-delete column (required by latest app code)
ALTER TABLE user_profile_bank_card
  ADD COLUMN deleted_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Soft delete flag: 1 deleted' AFTER default_flag;

CREATE INDEX idx_user_profile_bank_card_profile_deleted
  ON user_profile_bank_card (profile_id, deleted_flag);

-- F) Verify
SHOW COLUMNS FROM user_profile_bank_card;
SHOW INDEX FROM user_profile_bank_card;
