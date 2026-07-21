-- Append-only lender product list (replaces pk_product_snapshot write path + *_latest replace-all).
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

SET @schema_name = DATABASE();

CREATE TABLE IF NOT EXISTS pk_lender_product_list (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    credit_application_id BIGINT UNSIGNED NOT NULL COMMENT 'Local credit_application.id',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Mobile number',
    apply_id VARCHAR(64) NOT NULL COMMENT 'Platform credit apply id',
    credit_apply_no VARCHAR(64) NULL COMMENT 'Lender credit apply number',
    lender_user_id VARCHAR(64) NULL COMMENT 'Lender user ID',
    credit_status VARCHAR(32) NULL COMMENT 'Mapped/public credit status at fetch time',
    product_status VARCHAR(32) NOT NULL COMMENT 'READY / PENDING / EMPTY',
    content_hash CHAR(64) NOT NULL COMMENT 'SHA-256 of normalized product tree',
    external_interaction_id BIGINT UNSIGNED NULL COMMENT 'external_interaction.id for /product/list',
    fetched_at DATETIME(3) NOT NULL COMMENT 'Fetch time from lender',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_pk_lender_product_list_apply_id (apply_id, id),
    KEY idx_pk_lender_product_list_credit_app (credit_application_id, id),
    KEY idx_pk_lender_product_list_profile_fetched (profile_id, fetched_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Append-only lender product list header';

CREATE TABLE IF NOT EXISTS pk_lender_product (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    product_list_id BIGINT UNSIGNED NOT NULL COMMENT 'pk_lender_product_list.id',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Mobile number',
    product_code VARCHAR(64) NOT NULL COMMENT 'Product code',
    min_amount DECIMAL(19,2) NULL COMMENT 'Minimum loan amount',
    max_amount DECIMAL(19,2) NULL COMMENT 'Maximum loan amount',
    comprehensive_rate_unit VARCHAR(8) NULL COMMENT 'Comprehensive rate unit',
    comprehensive_rate DECIMAL(19,8) NULL COMMENT 'Comprehensive rate',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_pk_lender_product_list_id (product_list_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Append-only lender products';

CREATE TABLE IF NOT EXISTS pk_lender_product_repay_method (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    product_id BIGINT UNSIGNED NOT NULL COMMENT 'pk_lender_product.id',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Mobile number',
    repay_method VARCHAR(64) NOT NULL COMMENT 'Repayment method code',
    cycle_type VARCHAR(8) NULL COMMENT 'Cycle type',
    cycle_interval INT NULL COMMENT 'Cycle interval',
    cycle_count INT NULL COMMENT 'Cycle count',
    total_cycle_interval INT NULL COMMENT 'Total cycle interval',
    repay_method_type INT NULL COMMENT 'Repayment method type',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_pk_lender_product_repay_method_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Append-only lender product repayment methods';

CREATE TABLE IF NOT EXISTS pk_lender_product_uneven_rate (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    repay_method_id BIGINT UNSIGNED NOT NULL COMMENT 'pk_lender_product_repay_method.id',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Mobile number',
    term_num INT NOT NULL COMMENT 'Term number',
    repayment_rate DECIMAL(19,8) NOT NULL COMMENT 'Repayment rate',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_pk_lender_product_uneven_rate_repay (repay_method_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Append-only lender uneven repayment rates';

-- loan_quote.product_snapshot_id → product_list_id (semantics: pk_lender_product_list.id)
SET @col_snapshot = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_quote'
      AND COLUMN_NAME = 'product_snapshot_id'
);
SET @col_list = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_quote'
      AND COLUMN_NAME = 'product_list_id'
);
SET @ddl = IF(
    @col_snapshot > 0 AND @col_list = 0,
    'ALTER TABLE loan_quote CHANGE COLUMN product_snapshot_id product_list_id BIGINT UNSIGNED NULL COMMENT ''pk_lender_product_list.id''',
    'SELECT ''skip: loan_quote product_list_id already migrated'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
