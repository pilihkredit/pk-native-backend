-- Add last_lender_request_json / last_lender_response_json to credit_application.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

SET @db = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_application'
      AND COLUMN_NAME = 'last_lender_request_json'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE credit_application ADD COLUMN last_lender_request_json JSON NULL COMMENT ''Last lender credit API request audit JSON'' AFTER next_poll_at',
    'SELECT ''skip: credit_application.last_lender_request_json exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_application'
      AND COLUMN_NAME = 'last_lender_response_json'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE credit_application ADD COLUMN last_lender_response_json JSON NULL COMMENT ''Last lender credit API response data JSON'' AFTER last_lender_request_json',
    'SELECT ''skip: credit_application.last_lender_response_json exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
