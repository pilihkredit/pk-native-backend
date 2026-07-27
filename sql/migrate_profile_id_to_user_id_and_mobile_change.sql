-- migrate_profile_id_to_user_id_and_mobile_change.sql
-- Idempotent. Run before deploying app that expects user_id columns.
-- Active-mobile uniqueness uses a STORED generated column (MySQL 5.7.6+ / 8.0),
-- not a functional index expression (unavailable on many managed MySQL versions).

SET @schema_name = DATABASE();

-- Optional ops check before unique index:
-- SELECT mobile_no, COUNT(*) c FROM user_profile WHERE deleted_at IS NULL GROUP BY mobile_no HAVING c > 1;

-- ========== 0) Resolve loan_quote lender user_id naming conflict ==========
-- Existing loan_quote.user_id VARCHAR is lender user id; rename to lender_user_id
-- before profile_id -> user_id (BIGINT FK to user_profile.id).
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_quote' AND COLUMN_NAME='user_id'
    AND DATA_TYPE='varchar');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE loan_quote CHANGE COLUMN user_id lender_user_id VARCHAR(64) NULL COMMENT ''Lender user ID''',
  'SELECT ''skip: loan_quote.user_id -> lender_user_id'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== 1) Rename profile_id -> user_id ==========

-- credit_application
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='credit_application' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE credit_application CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: credit_application.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- credit_lender_status_query
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='credit_lender_status_query' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE credit_lender_status_query CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: credit_lender_status_query.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- data_subject_request
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='data_subject_request' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE data_subject_request CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: data_subject_request.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- loan_application
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_application' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE loan_application CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: loan_application.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- loan_lender_bill
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_lender_bill' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE loan_lender_bill CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: loan_lender_bill.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- loan_lender_history_order
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_lender_history_order' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE loan_lender_history_order CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: loan_lender_history_order.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- loan_lender_status_query
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_lender_status_query' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE loan_lender_status_query CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: loan_lender_status_query.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- loan_quote
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_quote' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE loan_quote CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: loan_quote.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ocr_vendor_call_log
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='ocr_vendor_call_log' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE ocr_vendor_call_log CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: ocr_vendor_call_log.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- pk_lender_product_list
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='pk_lender_product_list' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE pk_lender_product_list CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: pk_lender_product_list.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- repay_current_order
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='repay_current_order' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE repay_current_order CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: repay_current_order.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- repay_va_snapshot
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='repay_va_snapshot' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE repay_va_snapshot CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: repay_va_snapshot.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- repayment_trial_snapshot
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='repayment_trial_snapshot' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE repayment_trial_snapshot CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: repayment_trial_snapshot.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- sms_send_log
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='sms_send_log' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE sms_send_log CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: sms_send_log.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- tracking_event
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='tracking_event' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE tracking_event CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: tracking_event.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_agreement_record
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_agreement_record' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_agreement_record CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_agreement_record.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_consent_record
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_consent_record' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_consent_record CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_consent_record.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_contact_snapshot
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_contact_snapshot' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_contact_snapshot CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_contact_snapshot.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_device
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_device' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_device CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_device.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_device_other_info
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_device_other_info' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_device_other_info CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_device_other_info.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_lender_status_query
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_lender_status_query' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_lender_status_query CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_lender_status_query.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_af
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_af' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_af CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_profile_af.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_bank_card
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_bank_card' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_bank_card CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_profile_bank_card.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_contact
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_contact' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_contact CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_profile_contact.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_contacts
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_contacts' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_contacts CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_profile_contacts.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_identity
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_identity' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_identity CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_profile_identity.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_login_log
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_login_log' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_login_log CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_profile_login_log.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_personal
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_personal' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_personal CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_profile_personal.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_tongdun
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_tongdun' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_tongdun CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_profile_tongdun.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- user_profile_version
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_version' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_version CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: user_profile_version.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- whatsapp_send_log
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='whatsapp_send_log' AND COLUMN_NAME='profile_id');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE whatsapp_send_log CHANGE COLUMN profile_id user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: whatsapp_send_log.profile_id rename'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== 2) Add user_id where missing + backfill ==========

-- loan_quote_term.user_id
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_quote_term' AND COLUMN_NAME='user_id');
SET @ddl = IF(@col_exists=0,
  'ALTER TABLE loan_quote_term ADD COLUMN user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id'' AFTER quote_id',
  'SELECT ''skip: loan_quote_term.user_id add'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE loan_quote_term t
INNER JOIN loan_quote q ON q.id = t.quote_id
SET t.user_id = q.user_id
WHERE t.user_id IS NULL AND q.user_id IS NOT NULL;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_quote_term' AND COLUMN_NAME='user_id' AND IS_NULLABLE='YES');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE loan_quote_term MODIFY COLUMN user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: loan_quote_term.user_id not null'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- pk_lender_product.user_id
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='pk_lender_product' AND COLUMN_NAME='user_id');
SET @ddl = IF(@col_exists=0,
  'ALTER TABLE pk_lender_product ADD COLUMN user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id'' AFTER product_list_id',
  'SELECT ''skip: pk_lender_product.user_id add'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE pk_lender_product p
INNER JOIN pk_lender_product_list l ON l.id = p.product_list_id
SET p.user_id = l.user_id
WHERE p.user_id IS NULL;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='pk_lender_product' AND COLUMN_NAME='user_id' AND IS_NULLABLE='YES');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE pk_lender_product MODIFY COLUMN user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: pk_lender_product.user_id not null'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- pk_lender_product_repay_method.user_id
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='pk_lender_product_repay_method' AND COLUMN_NAME='user_id');
SET @ddl = IF(@col_exists=0,
  'ALTER TABLE pk_lender_product_repay_method ADD COLUMN user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id'' AFTER product_id',
  'SELECT ''skip: pk_lender_product_repay_method.user_id add'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE pk_lender_product_repay_method c
INNER JOIN pk_lender_product p ON p.id = c.product_id
SET c.user_id = p.user_id
WHERE c.user_id IS NULL;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='pk_lender_product_repay_method' AND COLUMN_NAME='user_id' AND IS_NULLABLE='YES');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE pk_lender_product_repay_method MODIFY COLUMN user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: pk_lender_product_repay_method.user_id not null'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- pk_lender_product_uneven_rate.user_id
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='pk_lender_product_uneven_rate' AND COLUMN_NAME='user_id');
SET @ddl = IF(@col_exists=0,
  'ALTER TABLE pk_lender_product_uneven_rate ADD COLUMN user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id'' AFTER repay_method_id',
  'SELECT ''skip: pk_lender_product_uneven_rate.user_id add'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE pk_lender_product_uneven_rate c
INNER JOIN pk_lender_product_repay_method rm ON rm.id = c.repay_method_id
INNER JOIN pk_lender_product p ON p.id = rm.product_id
SET c.user_id = p.user_id
WHERE c.user_id IS NULL;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='pk_lender_product_uneven_rate' AND COLUMN_NAME='user_id' AND IS_NULLABLE='YES');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE pk_lender_product_uneven_rate MODIFY COLUMN user_id BIGINT UNSIGNED NOT NULL COMMENT ''user_profile.id''',
  'SELECT ''skip: pk_lender_product_uneven_rate.user_id not null'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- external_interaction.user_id nullable (audit)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='external_interaction' AND COLUMN_NAME='user_id');
SET @ddl = IF(@col_exists=0,
  'ALTER TABLE external_interaction ADD COLUMN user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id when known'' AFTER mobile_no',
  'SELECT ''skip: external_interaction.user_id add'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- external_interaction_callback.user_id nullable (audit)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='external_interaction_callback' AND COLUMN_NAME='user_id');
SET @ddl = IF(@col_exists=0,
  'ALTER TABLE external_interaction_callback ADD COLUMN user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id when known'' AFTER mobile_no',
  'SELECT ''skip: external_interaction_callback.user_id add'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== 3) DROP account-owner mobile_no ==========

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_version' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_version DROP COLUMN mobile_no',
  'SELECT ''skip: user_profile_version.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_identity' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_identity DROP COLUMN mobile_no',
  'SELECT ''skip: user_profile_identity.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_contacts' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_contacts DROP COLUMN mobile_no',
  'SELECT ''skip: user_profile_contacts.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_contact' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_contact DROP COLUMN mobile_no',
  'SELECT ''skip: user_profile_contact.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_bank_card' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_bank_card DROP COLUMN mobile_no',
  'SELECT ''skip: user_profile_bank_card.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_login_log' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_login_log DROP COLUMN mobile_no',
  'SELECT ''skip: user_profile_login_log.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_af' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_af DROP COLUMN mobile_no',
  'SELECT ''skip: user_profile_af.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_tongdun' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_tongdun DROP COLUMN mobile_no',
  'SELECT ''skip: user_profile_tongdun.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile_personal' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_profile_personal DROP COLUMN mobile_no',
  'SELECT ''skip: user_profile_personal.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_lender_status_query' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_lender_status_query DROP COLUMN mobile_no',
  'SELECT ''skip: user_lender_status_query.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_agreement_record' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE user_agreement_record DROP COLUMN mobile_no',
  'SELECT ''skip: user_agreement_record.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='credit_application' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE credit_application DROP COLUMN mobile_no',
  'SELECT ''skip: credit_application.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='credit_lender_status_query' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE credit_lender_status_query DROP COLUMN mobile_no',
  'SELECT ''skip: credit_lender_status_query.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='pk_lender_product_list' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE pk_lender_product_list DROP COLUMN mobile_no',
  'SELECT ''skip: pk_lender_product_list.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='pk_lender_product' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE pk_lender_product DROP COLUMN mobile_no',
  'SELECT ''skip: pk_lender_product.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='pk_lender_product_repay_method' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE pk_lender_product_repay_method DROP COLUMN mobile_no',
  'SELECT ''skip: pk_lender_product_repay_method.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='pk_lender_product_uneven_rate' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE pk_lender_product_uneven_rate DROP COLUMN mobile_no',
  'SELECT ''skip: pk_lender_product_uneven_rate.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_quote' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE loan_quote DROP COLUMN mobile_no',
  'SELECT ''skip: loan_quote.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_quote_term' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE loan_quote_term DROP COLUMN mobile_no',
  'SELECT ''skip: loan_quote_term.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_application' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE loan_application DROP COLUMN mobile_no',
  'SELECT ''skip: loan_application.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_lender_status_query' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE loan_lender_status_query DROP COLUMN mobile_no',
  'SELECT ''skip: loan_lender_status_query.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_lender_history_order' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE loan_lender_history_order DROP COLUMN mobile_no',
  'SELECT ''skip: loan_lender_history_order.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='loan_lender_bill' AND COLUMN_NAME='mobile_no');
SET @ddl = IF(@col_exists>0,
  'ALTER TABLE loan_lender_bill DROP COLUMN mobile_no',
  'SELECT ''skip: loan_lender_bill.mobile_no drop'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== 4) user_mobile_change_log ==========
CREATE TABLE IF NOT EXISTS user_mobile_change_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'user_profile.id',
  partner_user_id VARCHAR(64) NULL COMMENT 'Partner user identifier at change time',
  old_mobile_no VARCHAR(32) NOT NULL COMMENT 'Previous mobile number',
  new_mobile_no VARCHAR(32) NOT NULL COMMENT 'New mobile number',
  status VARCHAR(32) NOT NULL COMMENT 'SUCCESS / FAILED',
  operator_type VARCHAR(32) NOT NULL COMMENT 'USER / ADMIN / SYSTEM',
  face_ticket_id VARCHAR(64) NULL COMMENT 'Optional face verification ticket',
  otp_ticket_id VARCHAR(64) NULL COMMENT 'Optional OTP ticket',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Change time',
  PRIMARY KEY (id),
  KEY idx_user_mobile_change_log_user_created (user_id, created_at),
  KEY idx_user_mobile_change_log_new_mobile (new_mobile_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Mobile number change audit log';

-- ========== 5) Active mobile uniqueness (generated column + unique index) ==========
-- Optional duplicate check before create:
-- SELECT mobile_no, COUNT(*) c FROM user_profile WHERE deleted_at IS NULL GROUP BY mobile_no HAVING c > 1;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile' AND COLUMN_NAME='active_mobile_no');
SET @ddl = IF(@col_exists=0,
  'ALTER TABLE user_profile ADD COLUMN active_mobile_no VARCHAR(32) GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN mobile_no ELSE NULL END) STORED COMMENT ''Active mobile for uniqueness; NULL when soft-deleted'' AFTER mobile_no',
  'SELECT ''skip: user_profile.active_mobile_no add'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='user_profile' AND INDEX_NAME='uk_user_profile_active_mobile');
SET @ddl = IF(@idx_exists=0,
  'CREATE UNIQUE INDEX uk_user_profile_active_mobile ON user_profile (active_mobile_no)',
  'SELECT ''skip: uk_user_profile_active_mobile'' AS migration_info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
