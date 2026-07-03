-- Create contract_file if absent and add lender contract-list audit columns.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

CREATE TABLE IF NOT EXISTS contract_file (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    loan_application_id BIGINT UNSIGNED NOT NULL COMMENT 'Loan application identifier',
    bill_no VARCHAR(64) NULL COMMENT 'Bill number',
    contract_type VARCHAR(64) NOT NULL COMMENT 'Contract type',
    contract_name VARCHAR(128) NOT NULL COMMENT 'Contract name',
    contract_url VARCHAR(1024) NOT NULL COMMENT 'Contract URL',
    file_ref VARCHAR(512) NULL COMMENT 'Object storage file reference',
    last_lender_request_json JSON NULL COMMENT 'Last lender contract list request JSON',
    last_lender_response_json JSON NULL COMMENT 'Last lender contract list response JSON',
    fetched_at DATETIME(3) NOT NULL COMMENT 'Fetch time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_contract_file_loan_type (loan_application_id, contract_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Loan contract file references';

SET @db = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'contract_file'
      AND COLUMN_NAME = 'last_lender_request_json'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE contract_file ADD COLUMN last_lender_request_json JSON NULL COMMENT ''Last lender contract list request JSON'' AFTER file_ref',
    'SELECT ''skip: contract_file.last_lender_request_json exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'contract_file'
      AND COLUMN_NAME = 'last_lender_response_json'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE contract_file ADD COLUMN last_lender_response_json JSON NULL COMMENT ''Last lender contract list response JSON'' AFTER last_lender_request_json',
    'SELECT ''skip: contract_file.last_lender_response_json exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
