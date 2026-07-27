-- Preserve submitted loan quotes: allow multiple rows per (apply_id, product_code, repay_method).
-- Draft (unreferenced) quotes remain updatable; referenced quotes are never overwritten.

SET @schema_name = DATABASE();

SET @idx_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'loan_quote'
    AND INDEX_NAME = 'uk_loan_quote_apply_product_repay'
);
SET @ddl = IF(
  @idx_exists > 0,
  'ALTER TABLE loan_quote DROP INDEX uk_loan_quote_apply_product_repay',
  'SELECT ''skip: uk_loan_quote_apply_product_repay missing'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'loan_quote'
    AND INDEX_NAME = 'idx_loan_quote_apply_product_repay_id'
);
SET @ddl = IF(
  @idx_exists = 0,
  'ALTER TABLE loan_quote ADD KEY idx_loan_quote_apply_product_repay_id (apply_id, product_code, repay_method, id)',
  'SELECT ''skip: idx_loan_quote_apply_product_repay_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
