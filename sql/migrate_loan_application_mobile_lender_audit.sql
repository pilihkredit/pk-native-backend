-- Add mobile number, credit apply id, request idempotency key, and lender audit JSON to loan_application.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

SET @schema_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_application'
      AND COLUMN_NAME = 'request_id'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN request_id VARCHAR(64) NULL COMMENT ''Client request identifier for idempotency'' AFTER loan_apply_id',
    'SELECT ''skip: loan_application.request_id exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_application'
      AND COLUMN_NAME = 'apply_id'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN apply_id VARCHAR(64) NULL COMMENT ''Credit application identifier'' AFTER request_id',
    'SELECT ''skip: loan_application.apply_id exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_application'
      AND COLUMN_NAME = 'mobile_no'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER apply_id',
    'SELECT ''skip: loan_application.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_application'
      AND COLUMN_NAME = 'lender_user_id'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN lender_user_id VARCHAR(64) NULL COMMENT ''Lender user identifier'' AFTER external_loan_apply_no',
    'SELECT ''skip: loan_application.lender_user_id exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_application'
      AND COLUMN_NAME = 'last_lender_request_json'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN last_lender_request_json JSON NULL COMMENT ''Last lender loan apply request JSON'' AFTER loan_purpose',
    'SELECT ''skip: loan_application.last_lender_request_json exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_application'
      AND COLUMN_NAME = 'last_lender_response_json'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN last_lender_response_json JSON NULL COMMENT ''Last lender loan apply response JSON'' AFTER last_lender_request_json',
    'SELECT ''skip: loan_application.last_lender_response_json exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE loan_application la
JOIN credit_application ca ON ca.id = la.credit_application_id
SET la.apply_id = ca.apply_id,
    la.mobile_no = ca.mobile_no
WHERE la.apply_id IS NULL OR la.mobile_no IS NULL;

UPDATE loan_application
SET request_id = loan_apply_id
WHERE request_id IS NULL;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_application'
      AND INDEX_NAME = 'uk_loan_application_request_id'
);
SET @sql = IF(
    @idx_exists = 0,
    'ALTER TABLE loan_application ADD UNIQUE KEY uk_loan_application_request_id (request_id)',
    'SELECT ''skip: uk_loan_application_request_id exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_application'
      AND INDEX_NAME = 'idx_loan_application_apply_id_lookup'
);
SET @sql = IF(
    @idx_exists = 0,
    'ALTER TABLE loan_application ADD KEY idx_loan_application_apply_id_lookup (apply_id)',
    'SELECT ''skip: idx_loan_application_apply_id_lookup exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_application'
      AND INDEX_NAME = 'idx_loan_application_mobile_no'
);
SET @sql = IF(
    @idx_exists = 0,
    'ALTER TABLE loan_application ADD KEY idx_loan_application_mobile_no (mobile_no)',
    'SELECT ''skip: idx_loan_application_mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
