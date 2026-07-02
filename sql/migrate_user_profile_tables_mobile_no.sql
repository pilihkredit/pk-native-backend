-- Add mobile_no to user_profile module tables.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). No MariaDB-only IF NOT EXISTS syntax.
-- Safe to re-run: skips ADD COLUMN / CREATE INDEX when already present.

SET @db = DATABASE();

-- ---------------------------------------------------------------------------
-- user_profile_version
-- ---------------------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile_version'
      AND COLUMN_NAME = 'mobile_no'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_version ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER profile_id',
    'SELECT ''skip: user_profile_version.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE user_profile_version v
JOIN user_profile up ON up.id = v.profile_id
SET v.mobile_no = up.mobile_no
WHERE v.mobile_no IS NULL OR v.mobile_no = '';

ALTER TABLE user_profile_version
    MODIFY mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number';

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile_version'
      AND INDEX_NAME = 'idx_user_profile_version_mobile_no'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_user_profile_version_mobile_no ON user_profile_version (mobile_no)',
    'SELECT ''skip: idx_user_profile_version_mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- user_identity_asset
-- ---------------------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_identity_asset'
      AND COLUMN_NAME = 'mobile_no'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_identity_asset ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER profile_id',
    'SELECT ''skip: user_identity_asset.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE user_identity_asset a
JOIN user_profile up ON up.id = a.profile_id
SET a.mobile_no = up.mobile_no
WHERE a.mobile_no IS NULL OR a.mobile_no = '';

ALTER TABLE user_identity_asset
    MODIFY mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number';

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_identity_asset'
      AND INDEX_NAME = 'idx_user_identity_asset_mobile_no'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_user_identity_asset_mobile_no ON user_identity_asset (mobile_no)',
    'SELECT ''skip: idx_user_identity_asset_mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- user_profile_identity
-- ---------------------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile_identity'
      AND COLUMN_NAME = 'mobile_no'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_identity ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER profile_id',
    'SELECT ''skip: user_profile_identity.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE user_profile_identity t
JOIN user_profile up ON up.id = t.profile_id
SET t.mobile_no = up.mobile_no
WHERE t.mobile_no IS NULL OR t.mobile_no = '';

ALTER TABLE user_profile_identity
    MODIFY mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number';

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile_identity'
      AND INDEX_NAME = 'idx_user_profile_identity_mobile_no'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_user_profile_identity_mobile_no ON user_profile_identity (mobile_no)',
    'SELECT ''skip: idx_user_profile_identity_mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- user_profile_contacts
-- ---------------------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile_contacts'
      AND COLUMN_NAME = 'mobile_no'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_contacts ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER profile_id',
    'SELECT ''skip: user_profile_contacts.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE user_profile_contacts t
JOIN user_profile up ON up.id = t.profile_id
SET t.mobile_no = up.mobile_no
WHERE t.mobile_no IS NULL OR t.mobile_no = '';

ALTER TABLE user_profile_contacts
    MODIFY mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number';

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile_contacts'
      AND INDEX_NAME = 'idx_user_profile_contacts_mobile_no'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_user_profile_contacts_mobile_no ON user_profile_contacts (mobile_no)',
    'SELECT ''skip: idx_user_profile_contacts_mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- user_profile_contact
-- ---------------------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile_contact'
      AND COLUMN_NAME = 'mobile_no'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_contact ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER profile_id',
    'SELECT ''skip: user_profile_contact.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE user_profile_contact t
JOIN user_profile up ON up.id = t.profile_id
SET t.mobile_no = up.mobile_no
WHERE t.mobile_no IS NULL OR t.mobile_no = '';

ALTER TABLE user_profile_contact
    MODIFY mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number';

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile_contact'
      AND INDEX_NAME = 'idx_user_profile_contact_mobile_no'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_user_profile_contact_mobile_no ON user_profile_contact (mobile_no)',
    'SELECT ''skip: idx_user_profile_contact_mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- user_profile_bank_card
-- ---------------------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile_bank_card'
      AND COLUMN_NAME = 'mobile_no'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_bank_card ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER profile_id',
    'SELECT ''skip: user_profile_bank_card.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE user_profile_bank_card t
JOIN user_profile up ON up.id = t.profile_id
SET t.mobile_no = up.mobile_no
WHERE t.mobile_no IS NULL OR t.mobile_no = '';

ALTER TABLE user_profile_bank_card
    MODIFY mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number';

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile_bank_card'
      AND INDEX_NAME = 'idx_user_profile_bank_card_mobile_no'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_user_profile_bank_card_mobile_no ON user_profile_bank_card (mobile_no)',
    'SELECT ''skip: idx_user_profile_bank_card_mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- user_profile_personal
-- ---------------------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile_personal'
      AND COLUMN_NAME = 'mobile_no'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_personal ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER profile_id',
    'SELECT ''skip: user_profile_personal.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE user_profile_personal t
JOIN user_profile up ON up.id = t.profile_id
SET t.mobile_no = up.mobile_no
WHERE t.mobile_no IS NULL OR t.mobile_no = '';

ALTER TABLE user_profile_personal
    MODIFY mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number';

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_profile_personal'
      AND INDEX_NAME = 'idx_user_profile_personal_mobile_no'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_user_profile_personal_mobile_no ON user_profile_personal (mobile_no)',
    'SELECT ''skip: idx_user_profile_personal_mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
