-- Create a dedicated latest snapshot table for lender loan status query results.
-- Do not backfill last_lender_* from loan_application: those columns audit loan apply,
-- not loan/apply/status.

CREATE TABLE IF NOT EXISTS loan_lender_status_query (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    loan_apply_id VARCHAR(64) NOT NULL COMMENT 'Loan application identifier',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number',
    lender_user_id VARCHAR(64) NULL COMMENT 'Lender user identifier',
    external_loan_apply_no VARCHAR(64) NULL COMMENT 'External loan application number',
    external_status VARCHAR(32) NULL COMMENT 'Lender loan status',
    bill_no VARCHAR(64) NULL COMMENT 'Bill number',
    apply_amt DECIMAL(18,2) NULL COMMENT 'Application amount from lender status query',
    pay_amount DECIMAL(18,2) NULL COMMENT 'Disbursement amount',
    pay_time DATETIME(3) NULL COMMENT 'Disbursement time',
    last_lender_request_json JSON NULL COMMENT 'Last lender loan status request JSON',
    last_lender_response_json JSON NULL COMMENT 'Last lender loan status response JSON',
    queried_at DATETIME(3) NOT NULL COMMENT 'Last lender status query time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_loan_lender_status_query_apply_id (loan_apply_id),
    KEY idx_loan_lender_status_query_profile (profile_id),
    KEY idx_loan_lender_status_query_mobile_no (mobile_no),
    KEY idx_loan_lender_status_query_external_no (external_loan_apply_no),
    KEY idx_loan_lender_status_query_bill_no (bill_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Latest lender loan status query results';
