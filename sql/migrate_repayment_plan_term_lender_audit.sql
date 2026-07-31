-- Add lender repay-plan audit columns to repayment_plan_term.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

SET @db = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'repayment_plan_term'
      AND COLUMN_NAME = 'last_lender_request_json'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE repayment_plan_term ADD COLUMN last_lender_request_json JSON NULL COMMENT ''Last lender repay plan request JSON'' AFTER last_repay_time',
    'SELECT ''skip: repayment_plan_term.last_lender_request_json exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'repayment_plan_term'
      AND COLUMN_NAME = 'last_lender_response_json'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE repayment_plan_term ADD COLUMN last_lender_response_json JSON NULL COMMENT ''Last lender repay plan response JSON'' AFTER last_lender_request_json',
    'SELECT ''skip: repayment_plan_term.last_lender_response_json exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
