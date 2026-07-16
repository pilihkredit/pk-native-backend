-- Drop unused mother_name columns from user_profile_identity.
-- Mother surname belongs to user_profile_personal / PERSONAL lender module, not IDENTITY.

SET @db := DATABASE();

SET @ddl := (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db
              AND TABLE_NAME = 'user_profile_identity'
              AND COLUMN_NAME = 'mother_name_ciphertext'
        ),
        'ALTER TABLE user_profile_identity DROP COLUMN mother_name_ciphertext',
        'SELECT ''skip: mother_name_ciphertext'' AS migration_info'
    )
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db
              AND TABLE_NAME = 'user_profile_identity'
              AND COLUMN_NAME = 'mother_name_nonce'
        ),
        'ALTER TABLE user_profile_identity DROP COLUMN mother_name_nonce',
        'SELECT ''skip: mother_name_nonce'' AS migration_info'
    )
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db
              AND TABLE_NAME = 'user_profile_identity'
              AND COLUMN_NAME = 'mother_name_tag'
        ),
        'ALTER TABLE user_profile_identity DROP COLUMN mother_name_tag',
        'SELECT ''skip: mother_name_tag'' AS migration_info'
    )
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
