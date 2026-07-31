-- Backfill external_interaction_callback.user_id from credit/loan applications,
-- and add lookup index. Safe to re-run.

SET @schema_name = DATABASE();

-- Index for user-scoped callback queries
SET @idx_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'external_interaction_callback'
      AND INDEX_NAME = 'idx_external_interaction_callback_user_created'
);
SET @ddl = IF(
    @idx_exists = 0,
    'ALTER TABLE external_interaction_callback ADD KEY idx_external_interaction_callback_user_created (user_id, created_at)',
    'SELECT ''skip: idx_external_interaction_callback_user_created exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- CREDIT_RESULT: business_id = credit_application.apply_id
UPDATE external_interaction_callback c
INNER JOIN credit_application a ON a.apply_id = c.business_id
SET c.user_id = a.user_id
WHERE c.business_type = 'CREDIT_RESULT'
  AND c.user_id IS NULL
  AND a.user_id IS NOT NULL;

-- LOAN_RESULT: business_id = loan_application.loan_apply_id
UPDATE external_interaction_callback c
INNER JOIN loan_application a ON a.loan_apply_id = c.business_id
SET c.user_id = a.user_id
WHERE c.business_type = 'LOAN_RESULT'
  AND c.user_id IS NULL
  AND a.user_id IS NOT NULL;
