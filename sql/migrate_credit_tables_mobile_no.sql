-- Add mobile_no to credit module tables.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). No MariaDB-only IF NOT EXISTS syntax.
-- Safe to re-run: skips ADD COLUMN / CREATE INDEX when already present.

SET @db = DATABASE();

-- ---------------------------------------------------------------------------
-- credit_application
-- ---------------------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_application'
      AND COLUMN_NAME = 'mobile_no'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE credit_application ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER profile_id',
    'SELECT ''skip: credit_application.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE credit_application ca
JOIN user_profile up ON up.id = ca.profile_id
SET ca.mobile_no = up.mobile_no
WHERE ca.mobile_no IS NULL OR ca.mobile_no = '';

ALTER TABLE credit_application
    MODIFY mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number';

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_application'
      AND INDEX_NAME = 'idx_credit_application_mobile_no'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_credit_application_mobile_no ON credit_application (mobile_no)',
    'SELECT ''skip: idx_credit_application_mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- credit_limit_snapshot
-- ---------------------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_limit_snapshot'
      AND COLUMN_NAME = 'mobile_no'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE credit_limit_snapshot ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER credit_application_id',
    'SELECT ''skip: credit_limit_snapshot.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE credit_limit_snapshot s
JOIN credit_application ca ON ca.id = s.credit_application_id
SET s.mobile_no = ca.mobile_no
WHERE s.mobile_no IS NULL OR s.mobile_no = '';

ALTER TABLE credit_limit_snapshot
    MODIFY mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number';

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_limit_snapshot'
      AND INDEX_NAME = 'idx_credit_limit_snapshot_mobile_no'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_credit_limit_snapshot_mobile_no ON credit_limit_snapshot (mobile_no)',
    'SELECT ''skip: idx_credit_limit_snapshot_mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- credit_status_history
-- ---------------------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_status_history'
      AND COLUMN_NAME = 'mobile_no'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE credit_status_history ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER credit_application_id',
    'SELECT ''skip: credit_status_history.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE credit_status_history h
JOIN credit_application ca ON ca.id = h.credit_application_id
SET h.mobile_no = ca.mobile_no
WHERE h.mobile_no IS NULL OR h.mobile_no = '';

ALTER TABLE credit_status_history
    MODIFY mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number';

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_status_history'
      AND INDEX_NAME = 'idx_credit_status_history_mobile_no'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_credit_status_history_mobile_no ON credit_status_history (mobile_no)',
    'SELECT ''skip: idx_credit_status_history_mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
