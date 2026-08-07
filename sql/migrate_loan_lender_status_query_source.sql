-- Add write-source on loan_lender_status_query (APP / JOB / CALLBACK).
SET @schema_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_lender_status_query'
      AND COLUMN_NAME = 'source'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE loan_lender_status_query
        ADD COLUMN source VARCHAR(32) NULL COMMENT ''Write source: APP / JOB / CALLBACK''
        AFTER external_interaction_callback_id',
    'SELECT ''skip: loan_lender_status_query.source'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
