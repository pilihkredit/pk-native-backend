-- Add mobile_no to external_interaction for lender request tracing by phone.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

SET @db = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'external_interaction'
      AND COLUMN_NAME = 'mobile_no'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE external_interaction ADD COLUMN mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'' AFTER business_id',
    'SELECT ''skip: external_interaction.mobile_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'external_interaction'
      AND INDEX_NAME = 'idx_external_interaction_mobile_created'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_external_interaction_mobile_created ON external_interaction (mobile_no, created_at)',
    'SELECT ''skip: idx_external_interaction_mobile_created exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
