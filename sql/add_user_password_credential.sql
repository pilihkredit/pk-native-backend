CREATE TABLE user_password_credential (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    password_hash VARCHAR(128) NOT NULL COMMENT 'BCrypt password hash',
    password_set_at DATETIME(3) NOT NULL COMMENT 'Password set time',
    failed_attempts INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Consecutive failed password login attempts',
    locked_until DATETIME(3) NULL COMMENT 'Password login lock expiry time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_password_profile (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User login password credential';
