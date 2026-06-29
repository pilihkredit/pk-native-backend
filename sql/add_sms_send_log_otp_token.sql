ALTER TABLE sms_send_log
    ADD COLUMN otp_token VARCHAR(128) NULL COMMENT 'OTP challenge token returned to client' AFTER device_no,
    ADD KEY idx_sms_send_log_otp_token (otp_token);
