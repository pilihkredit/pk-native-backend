-- loan_quote / loan_quote_term: latest-only by (apply_id, product_code, repay_method),
-- store lender epoch dates as BIGINT, audit via external_interaction_id.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

SET @schema_name = DATABASE();

-- loan_quote: drop product_list_id
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_quote' AND COLUMN_NAME = 'product_list_id'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE loan_quote DROP COLUMN product_list_id',
    'SELECT ''skip: product_list_id missing'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- drop JSON audit columns
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_quote' AND COLUMN_NAME = 'last_lender_request_json'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE loan_quote DROP COLUMN last_lender_request_json',
    'SELECT ''skip: last_lender_request_json missing'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_quote' AND COLUMN_NAME = 'last_lender_response_json'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE loan_quote DROP COLUMN last_lender_response_json',
    'SELECT ''skip: last_lender_response_json missing'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_quote' AND COLUMN_NAME = 'raw_response_json'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE loan_quote DROP COLUMN raw_response_json',
    'SELECT ''skip: raw_response_json missing'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- add profile_id
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_quote' AND COLUMN_NAME = 'profile_id'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_quote ADD COLUMN profile_id BIGINT UNSIGNED NULL COMMENT ''User profile identifier'' AFTER quote_no',
    'SELECT ''skip: profile_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- add coupon_id
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_quote' AND COLUMN_NAME = 'coupon_id'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_quote ADD COLUMN coupon_id BIGINT NULL COMMENT ''Lender couponId from trial request'' AFTER mobile_no',
    'SELECT ''skip: coupon_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- add external_interaction_id
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_quote' AND COLUMN_NAME = 'external_interaction_id'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_quote ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER coupon_id',
    'SELECT ''skip: external_interaction_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- unique latest key
SET @idx_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_quote'
      AND INDEX_NAME = 'uk_loan_quote_apply_product_repay'
);
SET @ddl = IF(
    @idx_exists = 0,
    'ALTER TABLE loan_quote ADD UNIQUE KEY uk_loan_quote_apply_product_repay (apply_id, product_code, repay_method)',
    'SELECT ''skip: uk_loan_quote_apply_product_repay exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- profile index
SET @idx_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_quote'
      AND INDEX_NAME = 'idx_loan_quote_profile_quoted'
);
SET @ddl = IF(
    @idx_exists = 0,
    'ALTER TABLE loan_quote ADD KEY idx_loan_quote_profile_quoted (profile_id, quoted_at)',
    'SELECT ''skip: idx_loan_quote_profile_quoted exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- term dates DATETIME -> BIGINT (store lender epoch ms)
-- MySQL: MODIFY column; if already BIGINT, skip via data_type check
SET @col_type = (
    SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_quote_term' AND COLUMN_NAME = 'value_date'
);
SET @ddl = IF(
    @col_type IS NOT NULL AND @col_type <> 'bigint',
    'ALTER TABLE loan_quote_term MODIFY COLUMN value_date BIGINT NULL COMMENT ''Lender valueDate epoch ms''',
    'SELECT ''skip: value_date already bigint or missing'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_type = (
    SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_quote_term' AND COLUMN_NAME = 'due_date'
);
SET @ddl = IF(
    @col_type IS NOT NULL AND @col_type <> 'bigint',
    'ALTER TABLE loan_quote_term MODIFY COLUMN due_date BIGINT NULL COMMENT ''Lender dueDate epoch ms''',
    'SELECT ''skip: due_date already bigint or missing'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_type = (
    SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_quote_term' AND COLUMN_NAME = 'grace_date'
);
SET @ddl = IF(
    @col_type IS NOT NULL AND @col_type <> 'bigint',
    'ALTER TABLE loan_quote_term MODIFY COLUMN grace_date BIGINT NULL COMMENT ''Lender graceDate epoch ms''',
    'SELECT ''skip: grace_date already bigint or missing'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- loan_application.quote_id nullable
SET @col_nullable = (
    SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'quote_id'
);
SET @ddl = IF(
    @col_nullable = 'NO',
    'ALTER TABLE loan_application MODIFY COLUMN quote_id BIGINT UNSIGNED NULL COMMENT ''Quote identifier''',
    'SELECT ''skip: quote_id already nullable or missing'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- loan_application.quote_no
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_application' AND COLUMN_NAME = 'quote_no'
);
SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_application ADD COLUMN quote_no VARCHAR(64) NULL COMMENT ''Client quoteNo association'' AFTER quote_id',
    'SELECT ''skip: quote_no exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
