-- user_profile_bank_card: allow multiple cards per profile.
-- Add surrogate id PK; profile_id becomes non-unique indexed column.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.
--
-- Handles broken intermediate state from older migration attempts:
--   PRIMARY KEY already dropped, but column `id` still missing.

SET @schema_name = DATABASE();

-- 1) Drop old PRIMARY KEY(profile_id) when still present
SET @pk_col = (
    SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_profile_bank_card'
      AND CONSTRAINT_NAME = 'PRIMARY'
    LIMIT 1
);
SET @ddl = IF(
    @pk_col = 'profile_id',
    'ALTER TABLE user_profile_bank_card DROP PRIMARY KEY',
    'SELECT ''skip: primary key already dropped or not profile_id'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) Add `id` if missing.
--    Prefer single-step AUTO_INCREMENT PRIMARY KEY when table has no PK.
--    If a non-id PK somehow exists, add nullable id then repair below.
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_bank_card' AND COLUMN_NAME = 'id'
);
SET @pk_col = (
    SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_profile_bank_card'
      AND CONSTRAINT_NAME = 'PRIMARY'
    LIMIT 1
);
SET @ddl = IF(
    @col_exists > 0,
    'SELECT ''skip: id exists'' AS migration_info',
    IF(
        @pk_col IS NULL,
        'ALTER TABLE user_profile_bank_card ADD COLUMN id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT ''Primary key'' FIRST',
        'ALTER TABLE user_profile_bank_card ADD COLUMN id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE KEY uk_user_profile_bank_card_id COMMENT ''Primary key'' FIRST'
    )
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3) If id exists but PRIMARY KEY missing, make id the PK
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_bank_card' AND COLUMN_NAME = 'id'
);
SET @pk_col = (
    SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_profile_bank_card'
      AND CONSTRAINT_NAME = 'PRIMARY'
    LIMIT 1
);
SET @ddl = IF(
    @col_exists > 0 AND @pk_col IS NULL,
    'ALTER TABLE user_profile_bank_card ADD PRIMARY KEY (id)',
    'SELECT ''skip: pk ok or id missing'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4) Ensure AUTO_INCREMENT on id
SET @extra = (
    SELECT EXTRA FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_bank_card' AND COLUMN_NAME = 'id'
    LIMIT 1
);
SET @pk_col = (
    SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_profile_bank_card'
      AND CONSTRAINT_NAME = 'PRIMARY'
    LIMIT 1
);
SET @ddl = IF(
    @pk_col = 'id' AND (@extra IS NULL OR @extra NOT LIKE '%auto_increment%'),
    'ALTER TABLE user_profile_bank_card MODIFY COLUMN id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT ''Primary key''',
    'SELECT ''skip: id auto_increment ok or pk not id'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 5) Drop leftover unique index from older migration attempts
SET @uk_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_profile_bank_card'
      AND INDEX_NAME = 'uk_user_profile_bank_card_id'
);
SET @ddl = IF(
    @uk_exists > 0,
    'ALTER TABLE user_profile_bank_card DROP INDEX uk_user_profile_bank_card_id',
    'SELECT ''skip: uk_user_profile_bank_card_id missing'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 6) Index profile_id
SET @idx_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_profile_bank_card'
      AND INDEX_NAME = 'idx_user_profile_bank_card_profile_id'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_user_profile_bank_card_profile_id ON user_profile_bank_card (profile_id)',
    'SELECT ''skip: idx_user_profile_bank_card_profile_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 7) Ensure profiles with cards but no default get one default (latest id)
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_bank_card' AND COLUMN_NAME = 'id'
);
SET @ddl = IF(
    @col_exists > 0,
    'UPDATE user_profile_bank_card c
     INNER JOIN (
         SELECT profile_id, MAX(id) AS max_id
         FROM user_profile_bank_card
         GROUP BY profile_id
         HAVING SUM(CASE WHEN default_flag = 1 THEN 1 ELSE 0 END) = 0
     ) missing ON missing.profile_id = c.profile_id AND missing.max_id = c.id
     SET c.default_flag = 1',
    'SELECT ''skip: default backfill needs id'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
