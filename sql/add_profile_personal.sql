CREATE TABLE IF NOT EXISTS user_profile_personal (
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    province_code VARCHAR(32) NOT NULL COMMENT 'Residential province code',
    city_code VARCHAR(32) NOT NULL COMMENT 'Residential city code',
    district_code VARCHAR(32) NOT NULL COMMENT 'Residential district code',
    address VARCHAR(512) NOT NULL COMMENT 'Residential street address',
    education_degree INT NOT NULL COMMENT 'Education degree code',
    mother_surname_ciphertext TEXT NOT NULL COMMENT 'AES-256-GCM encrypted mother surname ciphertext',
    mother_surname_nonce VARBINARY(12) NOT NULL COMMENT 'AES-GCM nonce for mother surname',
    mother_surname_tag VARBINARY(16) NOT NULL COMMENT 'AES-GCM authentication tag for mother surname',
    user_email VARCHAR(128) NULL COMMENT 'Optional user email',
    module_status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED' COMMENT 'Onboarding module status',
    last_request_id VARCHAR(64) NOT NULL COMMENT 'Last successful request id',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User personal basic information module';
