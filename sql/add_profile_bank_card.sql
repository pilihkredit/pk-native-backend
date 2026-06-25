CREATE TABLE IF NOT EXISTS user_profile_bank_card (
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    bank_code VARCHAR(64) NOT NULL COMMENT 'Bank code',
    card_no_hash CHAR(64) NOT NULL COMMENT 'SHA-256 hash of normalized card number',
    card_no_ciphertext TEXT NOT NULL COMMENT 'AES-256-GCM encrypted card number ciphertext',
    card_no_nonce VARBINARY(12) NOT NULL COMMENT 'AES-GCM nonce for card number',
    card_no_tag VARBINARY(16) NOT NULL COMMENT 'AES-GCM authentication tag for card number',
    verify_status VARCHAR(32) NOT NULL COMMENT 'Verification status',
    verify_error_code VARCHAR(32) NULL COMMENT 'Verification error code when failed',
    default_flag TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Default bank card flag',
    module_status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED' COMMENT 'Onboarding module status',
    last_request_id VARCHAR(64) NOT NULL COMMENT 'Last successful request id',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (profile_id),
    UNIQUE KEY uk_user_profile_bank_card_hash (card_no_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User bank card onboarding module';
