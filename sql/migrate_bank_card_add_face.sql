-- Add-bank-card face verification (every add attempt when a face baseline exists)

CREATE TABLE user_bank_card_add_face_verification (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    face_verify_token VARCHAR(64) NOT NULL COMMENT 'Opaque add-bank-card face verification token',
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'user_profile.id',
    partner_user_id VARCHAR(64) NULL COMMENT 'Partner user identifier',
    request_id VARCHAR(64) NOT NULL COMMENT 'Client request identifier for this face session',
    device_no VARCHAR(128) NOT NULL COMMENT 'Client device identifier at verify time',
    liveness_id VARCHAR(512) NOT NULL COMMENT 'Liveness provider transaction identifier',
    liveness_result VARCHAR(32) NULL COMMENT 'Liveness provider result',
    liveness_sequence_id VARCHAR(128) NULL COMMENT 'Liveness provider sequence identifier',
    baseline_type VARCHAR(32) NOT NULL COMMENT 'IDENTITY or PREVIOUS_MOBILE_CHANGE',
    baseline_face_encrypted_ref VARCHAR(1024) NOT NULL COMMENT 'Encrypted baseline face object reference',
    candidate_face_encrypted_ref VARCHAR(1024) NOT NULL COMMENT 'Encrypted captured face object reference',
    similarity DECIMAL(8,4) NULL COMMENT 'Face comparison similarity score',
    status VARCHAR(32) NOT NULL COMMENT 'FAILED, VERIFIED_PENDING, or CONSUMED',
    expires_at DATETIME(3) NOT NULL COMMENT 'Face verification token expiration time',
    consumed_at DATETIME(3) NULL COMMENT 'Time token was consumed by successful bank card save',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'Update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_bank_card_add_face_token (face_verify_token),
    KEY idx_bank_card_add_face_user_status (user_id, status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Add bank card face verification audit';
