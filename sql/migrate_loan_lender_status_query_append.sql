-- loan_lender_status_query: append-on-change (drop UNIQUE loan_apply_id),
-- keep latest lookup via (loan_apply_id, id).

SET @schema_name = DATABASE();

SET @uk_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_lender_status_query'
      AND INDEX_NAME = 'uk_loan_lender_status_query_apply_id'
);
SET @ddl = IF(
    @uk_exists > 0,
    'ALTER TABLE loan_lender_status_query DROP INDEX uk_loan_lender_status_query_apply_id',
    'SELECT ''skip: uk_loan_lender_status_query_apply_id missing'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_lender_status_query'
      AND INDEX_NAME = 'idx_loan_lender_status_query_apply_id'
);
SET @ddl = IF(
    @idx_exists = 0,
    'ALTER TABLE loan_lender_status_query ADD KEY idx_loan_lender_status_query_apply_id (loan_apply_id, id)',
    'SELECT ''skip: idx_loan_lender_status_query_apply_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
