-- Link latest loan status snapshot to inbound callback audit row.

SET @schema_name = DATABASE();

SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_lender_status_query'
      AND COLUMN_NAME = 'external_interaction_callback_id'
);
SET @ddl = IF(
    @column_exists = 0,
    'ALTER TABLE loan_lender_status_query
        ADD COLUMN external_interaction_callback_id BIGINT UNSIGNED NULL
            COMMENT ''external_interaction_callback.id for callback-driven updates''
            AFTER last_lender_response_json',
    'SELECT ''skip: external_interaction_callback_id exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
