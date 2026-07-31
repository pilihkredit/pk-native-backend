-- Create loan_lender_bill for lender bill list snapshots.
-- Each row is upserted by loan_apply_id (insert if absent, update if present).
-- Compatible with MySQL 5.7 / 8.0 (Aliyun RDS, DMS). Safe to re-run.

CREATE TABLE IF NOT EXISTS loan_lender_bill (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    loan_apply_id VARCHAR(64) NOT NULL COMMENT 'Loan application identifier',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number',
    external_loan_apply_no VARCHAR(64) NULL COMMENT 'External loan application number',
    lender_user_id VARCHAR(64) NULL COMMENT 'Lender user identifier',
    bill_no VARCHAR(64) NULL COMMENT 'Bill number',
    apply_amt DECIMAL(18,2) NULL COMMENT 'Application amount',
    bill_status VARCHAR(32) NULL COMMENT 'Lender bill status',
    term_due_date BIGINT NULL COMMENT 'Current term due date (epoch millis)',
    next_due_amount DECIMAL(18,2) NULL COMMENT 'Current term total due amount',
    last_lender_request_json JSON NULL COMMENT 'Last lender bill list request JSON',
    last_lender_response_json JSON NULL COMMENT 'Last lender bill list response item JSON',
    queried_at DATETIME(3) NOT NULL COMMENT 'Last lender bill list query time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_loan_lender_bill_apply_id (loan_apply_id),
    KEY idx_loan_lender_bill_profile (profile_id),
    KEY idx_loan_lender_bill_mobile_no (mobile_no),
    KEY idx_loan_lender_bill_bill_no (bill_no),
    KEY idx_loan_lender_bill_status (bill_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lender loan bill list snapshots';
