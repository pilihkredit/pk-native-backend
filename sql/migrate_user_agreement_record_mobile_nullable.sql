-- Allow anonymous agreement records without mobile_no.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

SET @db = DATABASE();

SET @col_nullable = (
    SELECT IS_NULLABLE
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'user_agreement_record'
      AND COLUMN_NAME = 'mobile_no'
);
SET @ddl = IF(
    @col_nullable = 'NO',
    'ALTER TABLE user_agreement_record MODIFY COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number; null when not logged in''',
    'SELECT ''skip: mobile_no already nullable'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
