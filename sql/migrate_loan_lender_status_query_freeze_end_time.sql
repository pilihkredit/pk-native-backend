-- Add lender freeze end time to latest loan status query snapshots.

SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'loan_lender_status_query'
      AND COLUMN_NAME = 'freeze_end_time'
);

SET @ddl = IF(
    @column_exists = 0,
    'ALTER TABLE loan_lender_status_query ADD COLUMN freeze_end_time BIGINT NULL COMMENT ''User freeze end time (epoch millis, only for REFUSED)'' AFTER pay_time',
    'SELECT ''skip: loan_lender_status_query.freeze_end_time exists'' AS migration_info'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
