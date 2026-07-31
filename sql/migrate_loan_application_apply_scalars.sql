-- loan_application: columnize apply request/response scalars,
-- drop JSON audit columns, add external_interaction_id.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.
-- product_code / repay_method: add nullable, backfill '', then NOT NULL.

SET @schema_name = DATABASE();

-- Drop last_lender_request_json
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'last_lender_request_json'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE loan_application DROP COLUMN last_lender_request_json',
    'SELECT ''skip: last_lender_request_json missing'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Drop last_lender_response_json
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'last_lender_response_json'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE loan_application DROP COLUMN last_lender_response_json',
    'SELECT ''skip: last_lender_response_json missing'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- external_interaction_id
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'external_interaction_id'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id for loan apply'' AFTER quote_no',
    'SELECT ''skip: external_interaction_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- product_code (nullable first)
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'product_code'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN product_code VARCHAR(64) NULL COMMENT ''Lender productCode'' AFTER apply_amt',
    'SELECT ''skip: product_code exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE loan_application SET product_code = '' WHERE product_code IS NULL;

SET @nullable = (
    SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'product_code'
);
SET @ddl = IF(
    @nullable = 'YES',
    'ALTER TABLE loan_application MODIFY COLUMN product_code VARCHAR(64) NOT NULL COMMENT ''Lender productCode''',
    'SELECT ''skip: product_code already NOT NULL'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- repay_method (nullable first)
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'repay_method'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN repay_method VARCHAR(64) NULL COMMENT ''Lender repayMethod'' AFTER product_code',
    'SELECT ''skip: repay_method exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE loan_application SET repay_method = '' WHERE repay_method IS NULL;

SET @nullable = (
    SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'repay_method'
);
SET @ddl = IF(
    @nullable = 'YES',
    'ALTER TABLE loan_application MODIFY COLUMN repay_method VARCHAR(64) NOT NULL COMMENT ''Lender repayMethod''',
    'SELECT ''skip: repay_method already NOT NULL'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- coupon_id
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'coupon_id'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN coupon_id BIGINT NULL COMMENT ''Lender couponId'' AFTER repay_method',
    'SELECT ''skip: coupon_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- lat
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'lat'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN lat DECIMAL(18,8) NULL COMMENT ''Request lat'' AFTER loan_purpose',
    'SELECT ''skip: lat exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- lng
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'lng'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN lng DECIMAL(18,8) NULL COMMENT ''Request lng'' AFTER lat',
    'SELECT ''skip: lng exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ip
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'ip'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN ip VARCHAR(32) NULL COMMENT ''Request ip'' AFTER lng',
    'SELECT ''skip: ip exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- address
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'address'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN address VARCHAR(512) NULL COMMENT ''Request address'' AFTER ip',
    'SELECT ''skip: address exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ad_id
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'ad_id'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN ad_id VARCHAR(128) NULL COMMENT ''Request adId'' AFTER address',
    'SELECT ''skip: ad_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
