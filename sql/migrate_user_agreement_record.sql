-- user_agreement_record: append-only agreement/consent snapshots for App.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

CREATE TABLE IF NOT EXISTS user_agreement_record (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    mobile_no VARCHAR(32) NULL COMMENT 'Account owner mobile number; null when not logged in',
    partner_user_id VARCHAR(64) NULL COMMENT 'Partner user identifier; optional when not logged in',
    device_no VARCHAR(128) NOT NULL COMMENT 'Device identifier',
    profile_id BIGINT UNSIGNED NULL COMMENT 'User profile identifier when logged in',
    agreement_type VARCHAR(64) NOT NULL COMMENT 'Agreement type code from client',
    agreed TINYINT(1) NULL COMMENT 'Agreement choice: 1 agree, 0 reject, NULL unknown/viewed',
    agreed_at DATETIME(3) NOT NULL COMMENT 'Client click timestamp for the agreement action',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_user_agreement_record_mobile_type_id (mobile_no, agreement_type, id),
    KEY idx_user_agreement_record_partner_user (partner_user_id),
    KEY idx_user_agreement_record_profile (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Append-only user agreement records';
