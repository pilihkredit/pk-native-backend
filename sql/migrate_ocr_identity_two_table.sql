-- Consolidate identity storage:
-- 1) Extend user_profile_identity with latest OCR/face asset fields (from user_identity_asset)
-- 2) Create ocr_vendor_call_log for all vendor call success/failure audits
-- 3) Backfill identity from latest user_identity_asset row per profile
-- Safe to re-run on MySQL 5.7 / 8.0.

SET @db = DATABASE();

-- ---------- user_profile_identity columns ----------
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'profile_version_id'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_identity ADD COLUMN profile_version_id BIGINT UNSIGNED NULL COMMENT ''Latest profile version identifier'' AFTER last_request_id',
    'SELECT ''skip: user_profile_identity.profile_version_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'mother_name_ciphertext'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_identity ADD COLUMN mother_name_ciphertext TEXT NULL COMMENT ''AES-256-GCM encrypted mother name ciphertext'' AFTER profile_version_id',
    'SELECT ''skip: user_profile_identity.mother_name_ciphertext exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'mother_name_nonce'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_identity ADD COLUMN mother_name_nonce VARBINARY(12) NULL COMMENT ''AES-GCM nonce for mother name'' AFTER mother_name_ciphertext',
    'SELECT ''skip: user_profile_identity.mother_name_nonce exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'mother_name_tag'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_identity ADD COLUMN mother_name_tag VARBINARY(16) NULL COMMENT ''AES-GCM authentication tag for mother name'' AFTER mother_name_nonce',
    'SELECT ''skip: user_profile_identity.mother_name_tag exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'id_card_image_encrypted_ref'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_identity ADD COLUMN id_card_image_encrypted_ref VARCHAR(512) NULL COMMENT ''Encrypted identity card image storage reference'' AFTER mother_name_tag',
    'SELECT ''skip: user_profile_identity.id_card_image_encrypted_ref exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'face_photo_image_encrypted_ref'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_identity ADD COLUMN face_photo_image_encrypted_ref VARCHAR(512) NULL COMMENT ''Encrypted face photo image storage reference'' AFTER id_card_image_encrypted_ref',
    'SELECT ''skip: user_profile_identity.face_photo_image_encrypted_ref exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'encryption_key_ref'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_identity ADD COLUMN encryption_key_ref VARCHAR(256) NULL COMMENT ''KMS reference for AES data encryption key'' AFTER face_photo_image_encrypted_ref',
    'SELECT ''skip: user_profile_identity.encryption_key_ref exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'identity_data_retention_until'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_identity ADD COLUMN identity_data_retention_until DATETIME(3) NULL COMMENT ''Planned retention end time for identity card number and mother name'' AFTER encryption_key_ref',
    'SELECT ''skip: user_profile_identity.identity_data_retention_until exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'biometric_image_retention_until'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_identity ADD COLUMN biometric_image_retention_until DATETIME(3) NULL COMMENT ''Planned retention end time for identity card and face images'' AFTER identity_data_retention_until',
    'SELECT ''skip: user_profile_identity.biometric_image_retention_until exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'ocr_channel'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_identity ADD COLUMN ocr_channel VARCHAR(64) NULL COMMENT ''OCR channel code'' AFTER biometric_image_retention_until',
    'SELECT ''skip: user_profile_identity.ocr_channel exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'ocr_result_json'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE user_profile_identity ADD COLUMN ocr_result_json JSON NULL COMMENT ''Latest OCR result JSON (sensitive fields encrypted in-place)'' AFTER ocr_channel',
    'SELECT ''skip: user_profile_identity.ocr_result_json exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Backfill from latest user_identity_asset when asset table still exists.
SET @asset_exists = (
    SELECT COUNT(*) FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_identity_asset'
);
SET @ddl = IF(
    @asset_exists > 0,
    'UPDATE user_profile_identity i
     INNER JOIN (
         SELECT a.*
         FROM user_identity_asset a
         INNER JOIN (
             SELECT profile_id, MAX(id) AS max_id
             FROM user_identity_asset
             GROUP BY profile_id
         ) latest ON latest.profile_id = a.profile_id AND latest.max_id = a.id
     ) src ON src.profile_id = i.profile_id
     SET i.profile_version_id = COALESCE(i.profile_version_id, src.profile_version_id),
         i.mother_name_ciphertext = COALESCE(i.mother_name_ciphertext, src.mother_name_ciphertext),
         i.mother_name_nonce = COALESCE(i.mother_name_nonce, src.mother_name_nonce),
         i.mother_name_tag = COALESCE(i.mother_name_tag, src.mother_name_tag),
         i.id_card_image_encrypted_ref = COALESCE(i.id_card_image_encrypted_ref, src.id_card_image_encrypted_ref),
         i.face_photo_image_encrypted_ref = COALESCE(i.face_photo_image_encrypted_ref, src.face_photo_image_encrypted_ref),
         i.encryption_key_ref = COALESCE(i.encryption_key_ref, src.encryption_key_ref),
         i.identity_data_retention_until = COALESCE(i.identity_data_retention_until, src.identity_data_retention_until),
         i.biometric_image_retention_until = COALESCE(i.biometric_image_retention_until, src.biometric_image_retention_until),
         i.ocr_channel = COALESCE(i.ocr_channel, src.ocr_channel),
         i.ocr_result_json = COALESCE(i.ocr_result_json, src.ocr_result_json)',
    'SELECT ''skip: user_identity_asset missing, no backfill'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Rename legacy asset table (keep data for rollback). App must stop writing to it.
SET @deprecated_exists = (
    SELECT COUNT(*) FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_identity_asset_deprecated'
);
SET @ddl = IF(
    @asset_exists > 0 AND @deprecated_exists = 0,
    'RENAME TABLE user_identity_asset TO user_identity_asset_deprecated',
    'SELECT ''skip: rename user_identity_asset'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS ocr_vendor_call_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NULL COMMENT 'User profile identifier',
    partner_user_id VARCHAR(64) NULL COMMENT 'Partner user identifier',
    mobile_no VARCHAR(32) NULL COMMENT 'Account owner mobile number',
    operation_type VARCHAR(32) NOT NULL COMMENT 'LICENSE_TOKEN / OCR_CHECK / LIVENESS_CHECK / FACE_COMPARE',
    channel VARCHAR(64) NOT NULL DEFAULT 'advanceAi' COMMENT 'OCR vendor channel',
    trace_id VARCHAR(64) NULL COMMENT 'Trace identifier',
    client_request_id VARCHAR(64) NULL COMMENT 'Client request id when available',
    status VARCHAR(32) NOT NULL COMMENT 'SUCCESS / VENDOR_ERROR / BIZ_REJECT / TIMEOUT / EXCEPTION',
    api_code VARCHAR(32) NULL COMMENT 'Platform API error code',
    vendor_code VARCHAR(64) NULL COMMENT 'Vendor response code',
    vendor_message VARCHAR(512) NULL COMMENT 'Vendor response message',
    score DECIMAL(18,6) NULL COMMENT 'Liveness score or face similarity',
    threshold DECIMAL(18,6) NULL COMMENT 'Business threshold used for pass/fail',
    endpoint VARCHAR(512) NULL COMMENT 'Vendor endpoint',
    http_status INT NULL COMMENT 'HTTP status code',
    duration_ms INT UNSIGNED NULL COMMENT 'Vendor call duration in milliseconds',
    request_json JSON NULL COMMENT 'Vendor request JSON (sensitive fields encrypted in-place)',
    response_json JSON NULL COMMENT 'Vendor response JSON (sensitive fields encrypted in-place)',
    request_image_encrypted_ref VARCHAR(512) NULL COMMENT 'Encrypted request image reference when applicable',
    response_image_encrypted_ref VARCHAR(512) NULL COMMENT 'Encrypted response image reference when applicable',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    PRIMARY KEY (id),
    KEY idx_ocr_vendor_call_log_profile_created (profile_id, created_at),
    KEY idx_ocr_vendor_call_log_created_op_status (created_at, operation_type, status),
    KEY idx_ocr_vendor_call_log_op_status_created (operation_type, status, created_at),
    KEY idx_ocr_vendor_call_log_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OCR vendor call audit log (success and failure)';
