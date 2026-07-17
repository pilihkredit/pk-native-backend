-- user_profile_login_log: latest login log per user for lender loginLog upsert sync

CREATE TABLE IF NOT EXISTS user_profile_login_log (
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number',
    login_type INT NOT NULL COMMENT 'Lender loginType: 1 password, 2 OTP, 4 face, 5 gesture',
    login_ip VARCHAR(32) NOT NULL COMMENT 'Login IP address',
    login_lat DECIMAL(10,7) NULL COMMENT 'Login latitude',
    login_lng DECIMAL(10,7) NULL COMMENT 'Login longitude',
    module_status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED' COMMENT 'Module status',
    last_request_id VARCHAR(64) NOT NULL COMMENT 'Last successful request id',
    last_lender_request_json JSON NULL COMMENT 'Last lender user/info/upsert request audit JSON',
    last_lender_response_json JSON NULL COMMENT 'Last lender user/info/upsert response data JSON',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (profile_id),
    KEY idx_user_profile_login_log_mobile_no (mobile_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User login log module';
