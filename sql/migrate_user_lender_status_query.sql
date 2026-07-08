-- Create user_lender_status_query for latest lender user status snapshots.
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

CREATE TABLE IF NOT EXISTS user_lender_status_query (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number',
    partner_user_id VARCHAR(64) NULL COMMENT 'PK partner user identifier',
    lender_user_id VARCHAR(64) NULL COMMENT 'Lender user identifier',
    user_loan_life_time_status INT NULL COMMENT 'Lender user loan lifecycle status code',
    user_loan_life_time_last_action INT NULL COMMENT 'Lender user loan lifecycle last action code',
    freeze_end_time BIGINT NULL COMMENT 'Lender freeze end time in epoch milliseconds',
    on_loan_count INT NULL COMMENT 'Active on-loan bill count from lender',
    credit_contract_expire_time BIGINT NULL COMMENT 'Credit contract expire time in epoch milliseconds',
    auto_credit TINYINT(1) NULL COMMENT 'Whether lender recommends automatic credit application',
    last_lender_request_json JSON NULL COMMENT 'Last lender user status request JSON',
    last_lender_response_json JSON NULL COMMENT 'Last lender user status response JSON',
    queried_at DATETIME(3) NOT NULL COMMENT 'Last lender user status query time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_lender_status_query_profile (profile_id),
    KEY idx_user_lender_status_query_partner_user (partner_user_id),
    KEY idx_user_lender_status_query_mobile_no (mobile_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Latest lender user status query results';

SET @schema_name = DATABASE();
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'user_lender_status_query'
      AND COLUMN_NAME = 'auto_credit'
);
SET @ddl = IF(
    @column_exists = 0,
    'ALTER TABLE user_lender_status_query ADD COLUMN auto_credit TINYINT(1) NULL COMMENT ''Whether lender recommends automatic credit application'' AFTER credit_contract_expire_time',
    'SELECT ''skip: user_lender_status_query.auto_credit exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
