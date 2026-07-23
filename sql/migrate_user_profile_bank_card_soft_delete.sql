-- user_profile_bank_card: soft-delete support.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

SET @schema_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_profile_bank_card'
      AND COLUMN_NAME = 'deleted_flag'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_bank_card ADD COLUMN deleted_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''Soft delete flag: 1 deleted'' AFTER default_flag',
    'SELECT ''skip: user_profile_bank_card.deleted_flag exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_profile_bank_card'
      AND INDEX_NAME = 'idx_user_profile_bank_card_profile_deleted'
);
SET @ddl = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_user_profile_bank_card_profile_deleted ON user_profile_bank_card (profile_id, deleted_flag)',
    'SELECT ''skip: idx_user_profile_bank_card_profile_deleted exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
