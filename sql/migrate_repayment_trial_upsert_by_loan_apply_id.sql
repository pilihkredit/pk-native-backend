-- Keep one repayment trial order per loan_apply_id (latest wins).
-- Safe to re-run.

SET @schema_name = DATABASE();

-- 1) Remove duplicate orders (keep newest id per loan_apply_id), cascading children.
DELETE d FROM repayment_trial_term_discount d
INNER JOIN repayment_trial_term t ON t.id = d.trial_term_id
INNER JOIN repayment_trial_order o ON o.id = t.trial_order_id
INNER JOIN (
    SELECT loan_apply_id, MAX(id) AS keep_id
    FROM repayment_trial_order
    GROUP BY loan_apply_id
    HAVING COUNT(*) > 1
) dup ON dup.loan_apply_id = o.loan_apply_id AND o.id <> dup.keep_id;

DELETE t FROM repayment_trial_term t
INNER JOIN repayment_trial_order o ON o.id = t.trial_order_id
INNER JOIN (
    SELECT loan_apply_id, MAX(id) AS keep_id
    FROM repayment_trial_order
    GROUP BY loan_apply_id
    HAVING COUNT(*) > 1
) dup ON dup.loan_apply_id = o.loan_apply_id AND o.id <> dup.keep_id;

DELETE v FROM repayment_trial_va_channel v
INNER JOIN repayment_trial_order o ON o.id = v.owner_id AND v.owner_type = 'ORDER'
INNER JOIN (
    SELECT loan_apply_id, MAX(id) AS keep_id
    FROM repayment_trial_order
    GROUP BY loan_apply_id
    HAVING COUNT(*) > 1
) dup ON dup.loan_apply_id = o.loan_apply_id AND o.id <> dup.keep_id;

DELETE o FROM repayment_trial_order o
INNER JOIN (
    SELECT loan_apply_id, MAX(id) AS keep_id
    FROM repayment_trial_order
    GROUP BY loan_apply_id
    HAVING COUNT(*) > 1
) dup ON dup.loan_apply_id = o.loan_apply_id AND o.id <> dup.keep_id;

-- 2) Drop orphan snapshots (no remaining orders)
DELETE v FROM repayment_trial_va_channel v
WHERE v.owner_type = 'SNAPSHOT'
  AND NOT EXISTS (
      SELECT 1 FROM repayment_trial_order o WHERE o.trial_id = v.owner_id
  );

DELETE s FROM repayment_trial_snapshot s
WHERE NOT EXISTS (
    SELECT 1 FROM repayment_trial_order o WHERE o.trial_id = s.id
);

-- 3) Unique key on loan_apply_id
SET @idx_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'repayment_trial_order'
      AND INDEX_NAME = 'uk_repayment_trial_order_loan_apply_id'
);
SET @ddl = IF(
    @idx_exists = 0,
    'ALTER TABLE repayment_trial_order ADD UNIQUE KEY uk_repayment_trial_order_loan_apply_id (loan_apply_id)',
    'SELECT ''skip: uk_repayment_trial_order_loan_apply_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
