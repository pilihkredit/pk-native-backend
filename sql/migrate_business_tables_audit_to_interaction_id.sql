-- Replace business-table lender/OCR JSON audit copies with id references.
-- Keep payloads only in external_interaction / ocr_vendor_call_log.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

SET @schema_name = DATABASE();

-- Helper pattern applied per table below.

-- ========== user_profile_identity ==========
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'ocr_result_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_identity DROP COLUMN ocr_result_json', 'SELECT ''skip: user_profile_identity.ocr_result_json missing'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_identity DROP COLUMN last_lender_request_json', 'SELECT ''skip: user_profile_identity.last_lender_request_json missing'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_identity DROP COLUMN last_lender_response_json', 'SELECT ''skip: user_profile_identity.last_lender_response_json missing'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'ocr_vendor_call_log_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_profile_identity ADD COLUMN ocr_vendor_call_log_id BIGINT UNSIGNED NULL COMMENT ''ocr_vendor_call_log.id for OCR_CHECK'' AFTER ocr_channel', 'SELECT ''skip: user_profile_identity.ocr_vendor_call_log_id exists'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_identity' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_profile_identity ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id for profile upsert'' AFTER ocr_vendor_call_log_id', 'SELECT ''skip: user_profile_identity.external_interaction_id exists'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== profile modules with last_lender_* ==========
-- user_profile_contacts
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_contacts' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_contacts DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_contacts' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_contacts DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_contacts' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_profile_contacts ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER last_request_id', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_bank_card
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_bank_card' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_bank_card DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_bank_card' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_bank_card DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_bank_card' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_profile_bank_card ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER last_request_id', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_login_log
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_login_log' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_login_log DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_login_log' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_login_log DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_login_log' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_profile_login_log ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER last_request_id', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_af
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_af' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_af DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_af' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_af DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_af' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_profile_af ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER request_id', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_tongdun
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_tongdun' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_tongdun DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_tongdun' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_tongdun DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_tongdun' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_profile_tongdun ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER request_id', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_personal
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_personal' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_personal DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_personal' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_profile_personal DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_profile_personal' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_profile_personal ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER last_request_id', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_lender_status_query
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_lender_status_query' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_lender_status_query DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_lender_status_query' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE user_lender_status_query DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'user_lender_status_query' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE user_lender_status_query ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER auto_credit', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- credit_application
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE credit_application DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE credit_application DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'credit_application' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE credit_application ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id for credit apply'' AFTER apply_no', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- loan_lender_status_query
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_lender_status_query' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE loan_lender_status_query DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_lender_status_query' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE loan_lender_status_query DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_lender_status_query' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE loan_lender_status_query ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id for status query'' AFTER freeze_end_time', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- loan_lender_history_order
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_lender_history_order' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE loan_lender_history_order DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_lender_history_order' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE loan_lender_history_order DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_lender_history_order' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE loan_lender_history_order ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER lender_create_time', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- loan_lender_bill
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_lender_bill' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE loan_lender_bill DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_lender_bill' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE loan_lender_bill DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'loan_lender_bill' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE loan_lender_bill ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER next_due_amount', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- contract_file
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'contract_file' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE contract_file DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'contract_file' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE contract_file DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'contract_file' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE contract_file ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER file_ref', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- repayment_plan_term
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'repayment_plan_term' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE repayment_plan_term DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'repayment_plan_term' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE repayment_plan_term DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'repayment_plan_term' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE repayment_plan_term ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER term_no', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- repay_va_snapshot
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'repay_va_snapshot' AND COLUMN_NAME = 'last_lender_request_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE repay_va_snapshot DROP COLUMN last_lender_request_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'repay_va_snapshot' AND COLUMN_NAME = 'last_lender_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE repay_va_snapshot DROP COLUMN last_lender_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'repay_va_snapshot' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE repay_va_snapshot ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER bank_channels_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- repayment_trial_snapshot
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'repayment_trial_snapshot' AND COLUMN_NAME = 'raw_response_json');
SET @ddl = IF(@col_exists > 0, 'ALTER TABLE repayment_trial_snapshot DROP COLUMN raw_response_json', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'repayment_trial_snapshot' AND COLUMN_NAME = 'external_interaction_id');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE repayment_trial_snapshot ADD COLUMN external_interaction_id BIGINT UNSIGNED NULL COMMENT ''external_interaction.id'' AFTER disabled_default_va_show', 'SELECT ''skip'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
