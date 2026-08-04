CREATE TABLE IF NOT EXISTS user_mobile_change_face_verification (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    face_verify_token VARCHAR(64) NOT NULL COMMENT 'Opaque mobile change face verification token',
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'user_profile.id',
    partner_user_id VARCHAR(64) NULL COMMENT 'Partner user identifier',
    request_id VARCHAR(64) NOT NULL COMMENT 'Client request identifier',
    device_no VARCHAR(128) NOT NULL COMMENT 'Client device identifier',
    liveness_id VARCHAR(512) NOT NULL COMMENT 'Liveness provider transaction identifier',
    liveness_result VARCHAR(32) NULL COMMENT 'Liveness provider result',
    liveness_sequence_id VARCHAR(128) NULL COMMENT 'Liveness provider sequence identifier',
    baseline_type VARCHAR(32) NOT NULL COMMENT 'IDENTITY or PREVIOUS_MOBILE_CHANGE',
    baseline_face_encrypted_ref VARCHAR(1024) NOT NULL COMMENT 'Encrypted baseline face object reference',
    candidate_face_encrypted_ref VARCHAR(1024) NOT NULL COMMENT 'Encrypted captured face object reference',
    similarity DECIMAL(8,4) NULL COMMENT 'Face comparison similarity score',
    status VARCHAR(32) NOT NULL COMMENT 'FAILED, VERIFIED_PENDING, or PROMOTED',
    otp_token VARCHAR(128) NULL COMMENT 'Bound mobile change OTP token',
    expires_at DATETIME(3) NOT NULL COMMENT 'Face verification token expiration time',
    promoted_at DATETIME(3) NULL COMMENT 'Time candidate became the next comparison baseline',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'Update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_mobile_change_face_token (face_verify_token),
    KEY idx_mobile_change_face_user_status (user_id, status, promoted_at),
    KEY idx_mobile_change_face_otp_token (otp_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Mobile number change face verification audit';
