CREATE TABLE sms_send_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NULL COMMENT 'User profile identifier when already registered',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Recipient mobile number',
    device_no VARCHAR(128) NOT NULL COMMENT 'Client device identifier at send time',
    otp_code VARCHAR(16) NOT NULL COMMENT 'OTP verification code sent to the provider',
    purpose VARCHAR(32) NOT NULL DEFAULT 'OTP' COMMENT 'SMS purpose code',
    provider_code VARCHAR(32) NULL COMMENT 'SMS gateway provider code',
    provider_message_id VARCHAR(128) NULL COMMENT 'Provider-side message identifier',
    provider_success TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether the provider accepted the SMS request',
    provider_error_code VARCHAR(64) NULL COMMENT 'Provider error code when failed',
    provider_error_message VARCHAR(512) NULL COMMENT 'Provider error message when failed',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Send attempt time',
    PRIMARY KEY (id),
    KEY idx_sms_send_log_mobile_created (mobile_no, created_at),
    KEY idx_sms_send_log_device_created (device_no, created_at),
    KEY idx_sms_send_log_profile_created (profile_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SMS OTP send audit log';
