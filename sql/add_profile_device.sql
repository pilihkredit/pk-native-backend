CREATE TABLE IF NOT EXISTS user_profile_device (
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    device_no VARCHAR(128) NOT NULL COMMENT 'Device identifier',
    system_platform VARCHAR(16) NOT NULL COMMENT 'System platform',
    client_app_name VARCHAR(64) NOT NULL COMMENT 'Client-reported application name',
    app_version VARCHAR(32) NOT NULL COMMENT 'Application version',
    package_name VARCHAR(128) NOT NULL COMMENT 'Application package name',
    ad_id VARCHAR(128) NULL COMMENT 'Advertising identifier',
    device_other_json JSON NULL COMMENT 'Extended device collection JSON',
    last_request_id VARCHAR(64) NOT NULL COMMENT 'Last successful request id',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (profile_id),
    KEY idx_user_profile_device_device_no (device_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Latest user device snapshot for onboarding';
