-- DEPRECATED: superseded by migrate_lender_product_list_append.sql (append-only pk_lender_product_*).
-- Kept only as historical migration record; do not run on new environments.
-- Normalized latest lender product list tables (one row-set per credit application).

CREATE TABLE IF NOT EXISTS pk_lender_product_list_latest (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    credit_application_id BIGINT UNSIGNED NOT NULL COMMENT 'Credit application identifier',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Mobile number',
    apply_id VARCHAR(64) NOT NULL COMMENT 'Credit apply ID',
    credit_apply_no VARCHAR(64) NULL COMMENT 'Lender credit apply number',
    lender_user_id VARCHAR(64) NULL COMMENT 'Lender user ID',
    credit_status VARCHAR(32) NOT NULL COMMENT 'Lender credit status',
    product_status VARCHAR(32) NOT NULL COMMENT 'Mapped product status',
    last_lender_request_json JSON NULL COMMENT 'Last lender product list request JSON',
    last_lender_response_json JSON NULL COMMENT 'Last lender product list response JSON',
    fetched_at DATETIME(3) NOT NULL COMMENT 'Last fetch time from lender',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_lender_product_list_credit (credit_application_id),
    KEY idx_pk_lender_product_list_profile (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Latest lender product list header';

CREATE TABLE IF NOT EXISTS pk_lender_product_latest (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    product_list_latest_id BIGINT UNSIGNED NOT NULL COMMENT 'Product list header identifier',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Mobile number',
    product_code VARCHAR(64) NOT NULL COMMENT 'Product code',
    product_name VARCHAR(128) NOT NULL COMMENT 'Product name',
    min_amount DECIMAL(19,2) NULL COMMENT 'Minimum loan amount',
    max_amount DECIMAL(19,2) NULL COMMENT 'Maximum loan amount',
    comprehensive_rate_unit VARCHAR(8) NULL COMMENT 'Comprehensive rate unit',
    comprehensive_rate DECIMAL(19,8) NULL COMMENT 'Comprehensive rate',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_pk_lender_product_list_latest (product_list_latest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Latest lender products';

CREATE TABLE IF NOT EXISTS pk_lender_product_repay_method_latest (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    product_latest_id BIGINT UNSIGNED NOT NULL COMMENT 'Product identifier',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Mobile number',
    repay_method VARCHAR(64) NOT NULL COMMENT 'Repayment method code',
    cycle_type VARCHAR(8) NULL COMMENT 'Cycle type',
    cycle_interval INT NULL COMMENT 'Cycle interval',
    cycle_count INT NULL COMMENT 'Cycle count',
    total_cycle_interval INT NULL COMMENT 'Total cycle interval',
    repay_method_type INT NULL COMMENT 'Repayment method type',
    uneven_bills_repayment_rate_json TEXT NULL COMMENT 'Raw uneven bills repayment rate JSON',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_pk_lender_repay_method_product (product_latest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Latest lender product repayment methods';

CREATE TABLE IF NOT EXISTS pk_lender_product_uneven_rate_latest (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    repay_method_latest_id BIGINT UNSIGNED NOT NULL COMMENT 'Repayment method identifier',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Mobile number',
    term_num INT NOT NULL COMMENT 'Term number',
    repayment_rate DECIMAL(19,8) NOT NULL COMMENT 'Repayment rate',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_pk_lender_uneven_rate_repay_method (repay_method_latest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Latest lender uneven repayment rates';
