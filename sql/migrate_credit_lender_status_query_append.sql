-- credit_lender_status_query: append-on-change (drop UNIQUE apply_id),
-- replace JSON audit columns with nullable external_interaction_id,
-- drop credit_status_history.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

SET @schema_name = DATABASE();

-- Drop UNIQUE(apply_id)
SET @idx_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'credit_lender_status_query'
      AND INDEX_NAME = 'uk_credit_lender_status_query_apply_id'
);
SET @ddl = IF(
    @idx_exists > 0,
    'ALTER TABLE credit_lender_status_query DROP INDEX uk_credit_lender_status_query_apply_id',
    'SELECT ''skip: uk_credit_lender_status_query_apply_id missing'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop last_lender_request_json
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'credit_lender_status_query'
      AND COLUMN_NAME = 'last_lender_request_json'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE credit_lender_status_query DROP COLUMN last_lender_request_json',
    'SELECT ''skip: last_lender_request_json missing'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop last_lender_response_json
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'credit_lender_status_query'
      AND COLUMN_NAME = 'last_lender_response_json'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE credit_lender_status_query DROP COLUMN last_lender_response_json',
    'SELECT ''skip: last_lender_response_json missing'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add external_interaction_id
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'credit_lender_status_query'
      AND COLUMN_NAME = 'external_interaction_id'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE credit_lender_status_query ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER borrow_amt_step_size',
    'SELECT ''skip: external_interaction_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Index for latest-by-apply_id reads
SET @idx_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'credit_lender_status_query'
      AND INDEX_NAME = 'idx_credit_lender_status_query_apply_id'
);
SET @ddl = IF(
    @idx_exists = 0,
    'ALTER TABLE credit_lender_status_query ADD KEY idx_credit_lender_status_query_apply_id (apply_id, id)',
    'SELECT ''skip: idx_credit_lender_status_query_apply_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DROP TABLE IF EXISTS credit_status_history;
