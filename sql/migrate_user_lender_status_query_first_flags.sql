-- Add firstLoan / firstCreditApply / firstLoanApply to user_lender_status_query.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

SET @schema_name = DATABASE();

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_lender_status_query'
      AND COLUMN_NAME = 'first_loan'
);
SET @ddl = IF(
    @column_exists = 0,
    'ALTER TABLE user_lender_status_query ADD COLUMN first_loan TINYINT(1) NULL COMMENT ''Whether this is the user first loan'' AFTER freeze_end_time',
    'SELECT ''skip: user_lender_status_query.first_loan exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_lender_status_query'
      AND COLUMN_NAME = 'first_credit_apply'
);
SET @ddl = IF(
    @column_exists = 0,
    'ALTER TABLE user_lender_status_query ADD COLUMN first_credit_apply TINYINT(1) NULL COMMENT ''Whether this is the first credit / pre-approval application'' AFTER first_loan',
    'SELECT ''skip: user_lender_status_query.first_credit_apply exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_lender_status_query'
      AND COLUMN_NAME = 'first_loan_apply'
);
SET @ddl = IF(
    @column_exists = 0,
    'ALTER TABLE user_lender_status_query ADD COLUMN first_loan_apply TINYINT(1) NULL COMMENT ''Whether this is the first withdrawal / loan application'' AFTER first_credit_apply',
    'SELECT ''skip: user_lender_status_query.first_loan_apply exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
