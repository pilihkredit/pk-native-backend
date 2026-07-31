-- Store raw client click epoch millis alongside converted agreed_at DATETIME.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

SET @schema_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_agreement_record'
      AND COLUMN_NAME = 'clicked_at_ms'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_agreement_record ADD COLUMN clicked_at_ms BIGINT UNSIGNED NULL COMMENT ''Raw client click epoch milliseconds'' AFTER agreed_at',
    'SELECT ''skip: user_agreement_record.clicked_at_ms exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
