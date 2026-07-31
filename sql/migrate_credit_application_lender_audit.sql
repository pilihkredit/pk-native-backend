-- Add last_lender_request_json / last_lender_response_json to credit_application.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

SET @db = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_application'
      AND COLUMN_NAME = 'last_lender_request_json'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE credit_application ADD COLUMN last_lender_request_json JSON NULL COMMENT ''Last lender credit API request audit JSON'' AFTER next_poll_at',
    'SELECT ''skip: credit_application.last_lender_request_json exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db
      AND TABLE_NAME = 'credit_application'
      AND COLUMN_NAME = 'last_lender_response_json'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE credit_application ADD COLUMN last_lender_response_json JSON NULL COMMENT ''Last lender credit API response data JSON'' AFTER last_lender_request_json',
    'SELECT ''skip: credit_application.last_lender_response_json exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS credit_lender_status_query (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    apply_id VARCHAR(64) NOT NULL COMMENT 'PK credit application identifier used for lender status query',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number',
    partner_user_id VARCHAR(64) NULL COMMENT 'PK partner user identifier',
    lender_user_id VARCHAR(64) NULL COMMENT 'Lender user identifier',
    credit_apply_no VARCHAR(64) NULL COMMENT 'External credit application number',
    external_status VARCHAR(32) NULL COMMENT 'Lender credit status',
    credit_contract_expire_time BIGINT NULL COMMENT 'Lender credit contract expiration time in epoch milliseconds',
    freeze_end_time BIGINT NULL COMMENT 'Lender freeze end time in epoch milliseconds',
    risk_min_limit DECIMAL(18,2) NULL COMMENT 'Minimum available credit limit',
    risk_max_limit DECIMAL(18,2) NULL COMMENT 'Maximum available credit limit',
    psychological_credit_limit DECIMAL(18,2) NULL COMMENT 'Psychological credit limit',
    fake_credit_limit DECIMAL(18,2) NULL COMMENT 'Displayed fake credit limit',
    borrow_amt_step_size DECIMAL(18,2) NULL COMMENT 'Borrow amount step size',
    last_lender_request_json JSON NULL COMMENT 'Last lender credit status request JSON',
    last_lender_response_json JSON NULL COMMENT 'Last lender credit status response JSON',
    queried_at DATETIME(3) NOT NULL COMMENT 'Last lender status query time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_credit_lender_status_query_apply_id (apply_id),
    KEY idx_credit_lender_status_query_profile (profile_id),
    KEY idx_credit_lender_status_query_mobile_no (mobile_no),
    KEY idx_credit_lender_status_query_external_no (credit_apply_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Latest lender credit status query results';

INSERT INTO credit_lender_status_query (
    apply_id,
    profile_id,
    mobile_no,
    partner_user_id,
    lender_user_id,
    credit_apply_no,
    external_status,
    credit_contract_expire_time,
    freeze_end_time,
    risk_min_limit,
    risk_max_limit,
    psychological_credit_limit,
    fake_credit_limit,
    borrow_amt_step_size,
    last_lender_request_json,
    last_lender_response_json,
    queried_at
)
SELECT
    ca.apply_id,
    ca.profile_id,
    ca.mobile_no,
    up.partner_user_id,
    NULL,
    ca.external_credit_apply_no,
    ca.external_status,
    CASE
        WHEN s.contract_expire_at IS NULL THEN NULL
        ELSE UNIX_TIMESTAMP(s.contract_expire_at) * 1000
    END,
    CASE
        WHEN ca.freeze_end_at IS NULL THEN NULL
        ELSE UNIX_TIMESTAMP(ca.freeze_end_at) * 1000
    END,
    s.risk_min_limit,
    s.risk_max_limit,
    s.psychological_credit_limit,
    s.fake_credit_limit,
    s.borrow_amt_step_size,
    ca.last_lender_request_json,
    ca.last_lender_response_json,
    COALESCE(s.updated_at, ca.updated_at, CURRENT_TIMESTAMP(3))
FROM credit_limit_snapshot s
JOIN credit_application ca ON ca.id = s.credit_application_id
LEFT JOIN user_profile up ON up.id = ca.profile_id
ON DUPLICATE KEY UPDATE
    profile_id = VALUES(profile_id),
    mobile_no = VALUES(mobile_no),
    partner_user_id = VALUES(partner_user_id),
    credit_apply_no = VALUES(credit_apply_no),
    external_status = VALUES(external_status),
    credit_contract_expire_time = VALUES(credit_contract_expire_time),
    freeze_end_time = VALUES(freeze_end_time),
    risk_min_limit = VALUES(risk_min_limit),
    risk_max_limit = VALUES(risk_max_limit),
    psychological_credit_limit = VALUES(psychological_credit_limit),
    fake_credit_limit = VALUES(fake_credit_limit),
    borrow_amt_step_size = VALUES(borrow_amt_step_size),
    last_lender_request_json = VALUES(last_lender_request_json),
    last_lender_response_json = VALUES(last_lender_response_json),
    queried_at = VALUES(queried_at);

