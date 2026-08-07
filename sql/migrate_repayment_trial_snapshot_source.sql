-- Add write-source on repayment_trial_snapshot (APP / JOB / CALLBACK).
SET @schema_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'repayment_trial_snapshot'
      AND COLUMN_NAME = 'source'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE repayment_trial_snapshot
        ADD COLUMN source VARCHAR(32) NULL COMMENT ''Write source: APP / JOB / CALLBACK''
        AFTER external_interaction_id',
    'SELECT ''skip: repayment_trial_snapshot.source'' AS migration_info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
