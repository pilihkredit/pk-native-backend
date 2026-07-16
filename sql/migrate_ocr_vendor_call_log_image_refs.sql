-- Rename OCR vendor call log image columns to reflect stored content:
-- id_card_image_encrypted_ref  = encrypted OSS ref of identity card image
-- liveness_image_encrypted_ref = encrypted OSS ref of liveness/face image

SET @db := DATABASE();

SET @ddl := (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db
              AND TABLE_NAME = 'ocr_vendor_call_log'
              AND COLUMN_NAME = 'request_image_encrypted_ref'
        )
        AND NOT EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db
              AND TABLE_NAME = 'ocr_vendor_call_log'
              AND COLUMN_NAME = 'id_card_image_encrypted_ref'
        ),
        'ALTER TABLE ocr_vendor_call_log CHANGE COLUMN request_image_encrypted_ref id_card_image_encrypted_ref VARCHAR(512) NULL COMMENT ''Encrypted OSS reference of identity card image''',
        'SELECT ''skip: id_card_image_encrypted_ref'' AS migration_info'
    )
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db
              AND TABLE_NAME = 'ocr_vendor_call_log'
              AND COLUMN_NAME = 'response_image_encrypted_ref'
        )
        AND NOT EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db
              AND TABLE_NAME = 'ocr_vendor_call_log'
              AND COLUMN_NAME = 'liveness_image_encrypted_ref'
        ),
        'ALTER TABLE ocr_vendor_call_log CHANGE COLUMN response_image_encrypted_ref liveness_image_encrypted_ref VARCHAR(512) NULL COMMENT ''Encrypted OSS reference of liveness/face image''',
        'SELECT ''skip: liveness_image_encrypted_ref'' AS migration_info'
    )
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
