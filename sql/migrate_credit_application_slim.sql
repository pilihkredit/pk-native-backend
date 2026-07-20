-- Slim credit_application: drop status/poll fields; rename external_credit_apply_no -> apply_no.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

SET @db = DATABASE();

-- ---------------------------------------------------------------------------
-- rename external_credit_apply_no -> apply_no
-- ---------------------------------------------------------------------------
SET @col_old = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_application'
      AND COLUMN_NAME = 'external_credit_apply_no'
);
SET @col_new = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_application'
      AND COLUMN_NAME = 'apply_no'
);
SET @ddl = IF(
    @col_old > 0 AND @col_new = 0,
    'ALTER TABLE credit_application CHANGE COLUMN external_credit_apply_no apply_no VARCHAR(64) NULL COMMENT ''Lender credit application number''',
    'SELECT ''skip: credit_application.apply_no'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- drop indexes that depend on removed columns
-- ---------------------------------------------------------------------------
SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_application'
      AND INDEX_NAME = 'idx_credit_application_poll'
);
SET @ddl = IF(
    @idx_exists > 0,
    'ALTER TABLE credit_application DROP INDEX idx_credit_application_poll',
    'SELECT ''skip: idx_credit_application_poll'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_application'
      AND INDEX_NAME = 'idx_credit_application_profile_status'
);
SET @ddl = IF(
    @idx_exists > 0,
    'ALTER TABLE credit_application DROP INDEX idx_credit_application_profile_status',
    'SELECT ''skip: idx_credit_application_profile_status'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_application'
      AND INDEX_NAME = 'idx_credit_application_external_no'
);
SET @ddl = IF(
    @idx_exists > 0,
    'ALTER TABLE credit_application DROP INDEX idx_credit_application_external_no',
    'SELECT ''skip: idx_credit_application_external_no'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- drop columns
-- ---------------------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'profile_version_id'
);
SET @ddl = IF(@col_exists > 0,
    'ALTER TABLE credit_application DROP COLUMN profile_version_id',
    'SELECT ''skip: profile_version_id'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'status'
);
SET @ddl = IF(@col_exists > 0,
    'ALTER TABLE credit_application DROP COLUMN status',
    'SELECT ''skip: status'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'external_status'
);
SET @ddl = IF(@col_exists > 0,
    'ALTER TABLE credit_application DROP COLUMN external_status',
    'SELECT ''skip: external_status'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'last_error_code'
);
SET @ddl = IF(@col_exists > 0,
    'ALTER TABLE credit_application DROP COLUMN last_error_code',
    'SELECT ''skip: last_error_code'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'submitted_at'
);
SET @ddl = IF(@col_exists > 0,
    'ALTER TABLE credit_application DROP COLUMN submitted_at',
    'SELECT ''skip: submitted_at'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'finalized_at'
);
SET @ddl = IF(@col_exists > 0,
    'ALTER TABLE credit_application DROP COLUMN finalized_at',
    'SELECT ''skip: finalized_at'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'freeze_end_at'
);
SET @ddl = IF(@col_exists > 0,
    'ALTER TABLE credit_application DROP COLUMN freeze_end_at',
    'SELECT ''skip: freeze_end_at'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'next_poll_at'
);
SET @ddl = IF(@col_exists > 0,
    'ALTER TABLE credit_application DROP COLUMN next_poll_at',
    'SELECT ''skip: next_poll_at'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'version'
);
SET @ddl = IF(@col_exists > 0,
    'ALTER TABLE credit_application DROP COLUMN version',
    'SELECT ''skip: version'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- indexes after slim
-- ---------------------------------------------------------------------------
SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_application'
      AND INDEX_NAME = 'idx_credit_application_profile'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_credit_application_profile ON credit_application (profile_id)',
    'SELECT ''skip: idx_credit_application_profile'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_application'
      AND INDEX_NAME = 'idx_credit_application_apply_no'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_credit_application_apply_no ON credit_application (apply_no)',
    'SELECT ''skip: idx_credit_application_apply_no'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- refresh audit column comments (best-effort; skip if column missing)
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'last_lender_request_json'
);
SET @ddl = IF(@col_exists > 0,
    'ALTER TABLE credit_application MODIFY COLUMN last_lender_request_json JSON NULL COMMENT ''Last lender /v1/credit/apply request audit JSON''',
    'SELECT ''skip: last_lender_request_json comment'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'last_lender_response_json'
);
SET @ddl = IF(@col_exists > 0,
    'ALTER TABLE credit_application MODIFY COLUMN last_lender_response_json JSON NULL COMMENT ''Last lender /v1/credit/apply response data JSON''',
    'SELECT ''skip: last_lender_response_json comment'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
