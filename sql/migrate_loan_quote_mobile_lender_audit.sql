-- Add mobile number and lender audit JSON fields to loan trial quote tables.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

SET @schema_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_quote'
      AND COLUMN_NAME = 'mobile_no'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE loan_quote ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER credit_application_id',
    'SELECT ''skip: loan_quote.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_quote'
      AND COLUMN_NAME = 'last_lender_request_json'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE loan_quote ADD COLUMN last_lender_request_json JSON NULL COMMENT ''Last lender loan trial request JSON'' AFTER fee_json',
    'SELECT ''skip: loan_quote.last_lender_request_json exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_quote'
      AND COLUMN_NAME = 'last_lender_response_json'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE loan_quote ADD COLUMN last_lender_response_json JSON NULL COMMENT ''Last lender loan trial response JSON'' AFTER last_lender_request_json',
    'SELECT ''skip: loan_quote.last_lender_response_json exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE loan_quote q
JOIN credit_application ca ON ca.id = q.credit_application_id
SET q.mobile_no = ca.mobile_no
WHERE q.mobile_no IS NULL;

UPDATE loan_quote
SET last_lender_response_json = raw_response_json
WHERE last_lender_response_json IS NULL
  AND raw_response_json IS NOT NULL;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_quote'
      AND INDEX_NAME = 'idx_loan_quote_mobile_no'
);
SET @sql = IF(
    @idx_exists = 0,
    'ALTER TABLE loan_quote ADD KEY idx_loan_quote_mobile_no (mobile_no)',
    'SELECT ''skip: idx_loan_quote_mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_quote_term'
      AND COLUMN_NAME = 'mobile_no'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE loan_quote_term ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER quote_id',
    'SELECT ''skip: loan_quote_term.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE loan_quote_term t
JOIN loan_quote q ON q.id = t.quote_id
SET t.mobile_no = q.mobile_no
WHERE t.mobile_no IS NULL;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_quote_term'
      AND INDEX_NAME = 'idx_loan_quote_term_mobile_no'
);
SET @sql = IF(
    @idx_exists = 0,
    'ALTER TABLE loan_quote_term ADD KEY idx_loan_quote_term_mobile_no (mobile_no)',
    'SELECT ''skip: idx_loan_quote_term_mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
