-- whatsapp_send_log: WhatsApp OTP send audit (mirrors sms_send_log).
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

CREATE TABLE IF NOT EXISTS whatsapp_send_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NULL COMMENT 'User profile identifier when already registered',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Recipient mobile number',
    device_no VARCHAR(128) NOT NULL COMMENT 'Client device identifier at send time',
    otp_token VARCHAR(128) NULL COMMENT 'OTP challenge token returned to client',
    otp_code VARCHAR(16) NOT NULL COMMENT 'OTP verification code sent to the provider',
    purpose VARCHAR(32) NOT NULL DEFAULT 'OTP' COMMENT 'WhatsApp purpose code',
    provider_code VARCHAR(32) NULL COMMENT 'WhatsApp gateway provider code',
    provider_message_id VARCHAR(128) NULL COMMENT 'Provider-side message identifier',
    provider_success TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether the provider accepted the WhatsApp request',
    provider_error_code VARCHAR(64) NULL COMMENT 'Provider error code when failed',
    provider_error_message VARCHAR(512) NULL COMMENT 'Provider error message when failed',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Send attempt time',
    PRIMARY KEY (id),
    KEY idx_whatsapp_send_log_mobile_created (mobile_no, created_at),
    KEY idx_whatsapp_send_log_device_created (device_no, created_at),
    KEY idx_whatsapp_send_log_profile_created (profile_id, created_at),
    KEY idx_whatsapp_send_log_otp_token (otp_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='WhatsApp OTP send audit log';
