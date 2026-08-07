-- Rollback accidental loan_lender_history_order.source column.
-- Use after migrate_loan_lender_history_order_source.sql was applied by mistake.
SET @schema_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_lender_history_order'
      AND COLUMN_NAME = 'source'
);
SET @sql = IF(
    @col_exists > 0,
    'ALTER TABLE loan_lender_history_order DROP COLUMN source',
    'SELECT ''skip: loan_lender_history_order.source already absent'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
