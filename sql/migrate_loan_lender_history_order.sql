-- Create loan_lender_history_order for lender historical loan order snapshots.
-- Each row is upserted by loan_apply_id (insert if absent, update if present).
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

CREATE TABLE IF NOT EXISTS loan_lender_history_order (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    loan_apply_id VARCHAR(64) NOT NULL COMMENT 'Loan application identifier',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number',
    external_loan_apply_no VARCHAR(64) NULL COMMENT 'External loan application number',
    lender_user_id VARCHAR(64) NULL COMMENT 'Lender user identifier',
    external_status VARCHAR(32) NULL COMMENT 'Lender loan status',
    bill_no VARCHAR(64) NULL COMMENT 'Bill number',
    apply_amt DECIMAL(18,2) NULL COMMENT 'Application amount',
    pay_amount DECIMAL(18,2) NULL COMMENT 'Disbursement amount',
    pay_time DATETIME(3) NULL COMMENT 'Disbursement time',
    freeze_end_time BIGINT NULL COMMENT 'User freeze end time (epoch millis, only for REFUSED)',
    lender_create_time BIGINT NULL COMMENT 'Lender order creation time (epoch millis)',
    last_lender_request_json JSON NULL COMMENT 'Last lender loan history request JSON',
    last_lender_response_json JSON NULL COMMENT 'Last lender loan history response item JSON',
    queried_at DATETIME(3) NOT NULL COMMENT 'Last lender history query time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_loan_lender_history_order_apply_id (loan_apply_id),
    KEY idx_loan_lender_history_order_profile (profile_id),
    KEY idx_loan_lender_history_order_mobile_no (mobile_no),
    KEY idx_loan_lender_history_order_external_no (external_loan_apply_no),
    KEY idx_loan_lender_history_order_bill_no (bill_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lender historical loan order snapshots';
