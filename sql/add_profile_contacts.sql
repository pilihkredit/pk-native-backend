CREATE TABLE IF NOT EXISTS user_profile_contacts (
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    module_status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED' COMMENT 'Onboarding module status',
    last_request_id VARCHAR(64) NOT NULL COMMENT 'Last successful request id',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User emergency contacts module state';

CREATE TABLE IF NOT EXISTS user_profile_contact (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    sort_no INT UNSIGNED NOT NULL COMMENT 'Display and submission order',
    relationship INT NOT NULL COMMENT 'Contact relationship code',
    contact_name VARCHAR(128) NOT NULL COMMENT 'Contact name',
    contact_mobile VARCHAR(32) NOT NULL COMMENT 'Contact mobile number',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_profile_contact_sort (profile_id, sort_no),
    KEY idx_user_profile_contact_profile (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User emergency contact entries';
