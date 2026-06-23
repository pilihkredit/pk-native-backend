CREATE TABLE pk_provider (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    provider_code VARCHAR(32) NOT NULL COMMENT 'External provider code',
    provider_name VARCHAR(128) NOT NULL COMMENT 'External provider display name',
    status VARCHAR(32) NOT NULL COMMENT 'Record status',
    base_url VARCHAR(512) NOT NULL COMMENT 'External API base URL',
    callback_base_url VARCHAR(512) NOT NULL COMMENT 'Partner callback base URL',
    config_json JSON NULL COMMENT 'Extended configuration JSON',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_provider_code (provider_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='External PK provider configuration';

CREATE TABLE pk_api_credential (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    provider_code VARCHAR(32) NOT NULL COMMENT 'External provider code',
    client_id VARCHAR(128) NOT NULL COMMENT 'API client identifier',
    client_secret_ref VARCHAR(256) NOT NULL COMMENT 'KMS reference for API client secret',
    callback_client_id VARCHAR(128) NULL COMMENT 'Callback API client identifier',
    callback_secret_ref VARCHAR(256) NULL COMMENT 'KMS reference for callback client secret',
    effective_at DATETIME(3) NOT NULL COMMENT 'Credential effective time',
    expired_at DATETIME(3) NULL COMMENT 'Credential expiration time',
    status VARCHAR(32) NOT NULL COMMENT 'Record status',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_pk_api_credential_provider_status (provider_code, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='External PK API credential references';

CREATE TABLE ref_bank (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    bank_code VARCHAR(64) NOT NULL COMMENT 'Bank code',
    bank_name VARCHAR(128) NOT NULL COMMENT 'Bank name',
    bank_type INT NULL COMMENT 'Bank type',
    icon_url VARCHAR(512) NULL COMMENT 'Bank icon URL',
    status VARCHAR(32) NOT NULL COMMENT 'Record status',
    synced_at DATETIME(3) NULL COMMENT 'Last synchronization time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ref_bank_code (bank_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Cached bank reference data';

CREATE TABLE ref_area (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    area_code VARCHAR(64) NOT NULL COMMENT 'Area code',
    area_name VARCHAR(128) NOT NULL COMMENT 'Area name',
    parent_code VARCHAR(64) NULL COMMENT 'Parent area code',
    area_level TINYINT UNSIGNED NOT NULL COMMENT 'Area hierarchy level',
    status VARCHAR(32) NOT NULL COMMENT 'Record status',
    synced_at DATETIME(3) NULL COMMENT 'Last synchronization time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ref_area_code (area_code),
    KEY idx_ref_area_parent (parent_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Cached administrative area reference data';

CREATE TABLE user_profile (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    partner_user_id VARCHAR(64) NOT NULL COMMENT 'Partner user identifier',
    external_user_id VARCHAR(64) NULL COMMENT 'External PK user identifier',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Current mobile number',
    email VARCHAR(128) NULL COMMENT 'Email address',
    whats_app VARCHAR(32) NULL COMMENT 'WhatsApp number',
    current_profile_version_id BIGINT UNSIGNED NULL COMMENT 'Current profile version identifier',
    kyc_status VARCHAR(32) NOT NULL COMMENT 'KYC completion status',
    data_lifecycle_status VARCHAR(32) NOT NULL COMMENT 'Personal data lifecycle status',
    data_residency_country CHAR(2) NOT NULL DEFAULT 'ID' COMMENT 'Primary data residency country code',
    retention_policy_code VARCHAR(64) NOT NULL COMMENT 'Applied retention policy code',
    retention_until DATETIME(3) NULL COMMENT 'Planned retention end time',
    anonymized_at DATETIME(3) NULL COMMENT 'Anonymization completion time',
    last_synced_at DATETIME(3) NULL COMMENT 'Last profile sync success time',
    version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Optimistic lock version',
    deleted_at DATETIME(3) NULL COMMENT 'Soft deletion time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_profile_partner_user_id (partner_user_id),
    KEY idx_user_profile_external_user_id (external_user_id),
    KEY idx_user_profile_mobile_no (mobile_no),
    KEY idx_user_profile_lifecycle_retention (data_lifecycle_status, retention_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Current user profile master data';

CREATE TABLE sms_send_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NULL COMMENT 'User profile identifier when already registered',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Recipient mobile number',
    device_no VARCHAR(128) NOT NULL COMMENT 'Client device identifier at send time',
    otp_code VARCHAR(16) NOT NULL COMMENT 'OTP verification code sent to the provider',
    purpose VARCHAR(32) NOT NULL DEFAULT 'OTP' COMMENT 'SMS purpose code',
    provider_code VARCHAR(32) NULL COMMENT 'SMS gateway provider code',
    provider_message_id VARCHAR(128) NULL COMMENT 'Provider-side message identifier',
    provider_success TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether the provider accepted the SMS request',
    provider_error_code VARCHAR(64) NULL COMMENT 'Provider error code when failed',
    provider_error_message VARCHAR(512) NULL COMMENT 'Provider error message when failed',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Send attempt time',
    PRIMARY KEY (id),
    KEY idx_sms_send_log_mobile_created (mobile_no, created_at),
    KEY idx_sms_send_log_device_created (device_no, created_at),
    KEY idx_sms_send_log_profile_created (profile_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SMS OTP send audit log';

CREATE TABLE user_profile_version (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    version_no INT UNSIGNED NOT NULL COMMENT 'Profile version number',
    snapshot_hash CHAR(64) NOT NULL COMMENT 'Snapshot content hash',
    snapshot_json JSON NOT NULL COMMENT 'Profile snapshot JSON',
    completed_modules JSON NOT NULL COMMENT 'Completed profile module list',
    legal_basis VARCHAR(64) NOT NULL COMMENT 'Legal basis for processing this profile version',
    processing_purpose VARCHAR(128) NOT NULL COMMENT 'Processing purpose code',
    consent_no VARCHAR(64) NULL COMMENT 'Related consent record number',
    source VARCHAR(32) NOT NULL COMMENT 'Record source',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_profile_version_no (profile_id, version_no),
    KEY idx_user_profile_version_hash (profile_id, snapshot_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Immutable user profile snapshot versions';

CREATE TABLE user_identity_asset (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    profile_version_id BIGINT UNSIGNED NOT NULL COMMENT 'Profile version identifier',
    id_card_hash CHAR(64) NOT NULL COMMENT 'Identity card number hash',
    id_card_ciphertext TEXT NOT NULL COMMENT 'AES-256-GCM encrypted identity card number ciphertext',
    id_card_nonce VARBINARY(12) NOT NULL COMMENT 'AES-GCM nonce for identity card number',
    id_card_tag VARBINARY(16) NOT NULL COMMENT 'AES-GCM authentication tag for identity card number',
    full_name VARCHAR(128) NOT NULL COMMENT 'Full legal name',
    mother_name_ciphertext TEXT NULL COMMENT 'AES-256-GCM encrypted mother name ciphertext',
    mother_name_nonce VARBINARY(12) NULL COMMENT 'AES-GCM nonce for mother name',
    mother_name_tag VARBINARY(16) NULL COMMENT 'AES-GCM authentication tag for mother name',
    id_card_image_encrypted_ref VARCHAR(512) NOT NULL COMMENT 'Encrypted identity card image storage reference',
    face_photo_image_encrypted_ref VARCHAR(512) NOT NULL COMMENT 'Encrypted face photo image storage reference',
    encryption_key_ref VARCHAR(256) NOT NULL COMMENT 'KMS reference for AES data encryption key',
    identity_data_retention_until DATETIME(3) NULL COMMENT 'Planned retention end time for identity card number and mother name',
    identity_data_deleted_at DATETIME(3) NULL COMMENT 'Identity card number and mother name deletion or anonymization time',
    biometric_image_retention_until DATETIME(3) NULL COMMENT 'Planned retention end time for identity card and face images',
    biometric_image_deleted_at DATETIME(3) NULL COMMENT 'Identity card and face image hard deletion time',
    ocr_channel VARCHAR(64) NULL COMMENT 'OCR channel code',
    ocr_result_json JSON NULL COMMENT 'OCR result JSON',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_user_identity_asset_profile_version (profile_version_id),
    KEY idx_user_identity_asset_id_card_hash (id_card_hash),
    KEY idx_user_identity_asset_identity_retention (identity_data_retention_until),
    KEY idx_user_identity_asset_biometric_retention (biometric_image_retention_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User identity assets and OCR results';

CREATE TABLE user_bank_card (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    bank_code VARCHAR(64) NOT NULL COMMENT 'Bank code',
    card_no VARCHAR(64) NOT NULL COMMENT 'Bank card number',
    verify_status VARCHAR(32) NOT NULL COMMENT 'Verification status',
    verify_error_code VARCHAR(32) NULL COMMENT 'Verification error code',
    default_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Default flag',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_bank_card_no (card_no),
    KEY idx_user_bank_card_profile_default (profile_id, default_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User bank card verification records';

CREATE TABLE user_device_snapshot (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    profile_version_id BIGINT UNSIGNED NOT NULL COMMENT 'Profile version identifier',
    device_no VARCHAR(128) NOT NULL COMMENT 'Device identifier',
    system_platform VARCHAR(16) NOT NULL COMMENT 'System platform',
    app_name VARCHAR(64) NOT NULL COMMENT 'Application name',
    app_version VARCHAR(32) NOT NULL COMMENT 'Application version',
    ad_id VARCHAR(128) NULL COMMENT 'Advertising identifier',
    device_json JSON NOT NULL COMMENT 'Device data JSON',
    device_other_json JSON NULL COMMENT 'Extended device data JSON',
    consent_no VARCHAR(64) NULL COMMENT 'Related consent record number',
    retention_until DATETIME(3) NULL COMMENT 'Planned retention end time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_user_device_snapshot_profile_version (profile_version_id),
    KEY idx_user_device_snapshot_device_no (device_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User device snapshot records';

CREATE TABLE user_contact_snapshot (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    profile_version_id BIGINT UNSIGNED NOT NULL COMMENT 'Profile version identifier',
    relationship INT NOT NULL COMMENT 'Contact relationship code',
    contact_name VARCHAR(128) NOT NULL COMMENT 'Contact name',
    contact_mobile VARCHAR(32) NOT NULL COMMENT 'Contact mobile number',
    sort_no INT UNSIGNED NOT NULL COMMENT 'Sort number',
    consent_no VARCHAR(64) NULL COMMENT 'Related consent record number',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_user_contact_snapshot_profile_version (profile_version_id),
    KEY idx_user_contact_snapshot_mobile (contact_mobile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User contact snapshot records';

CREATE TABLE data_retention_policy (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    policy_code VARCHAR(64) NOT NULL COMMENT 'Retention policy code',
    business_domain VARCHAR(64) NOT NULL COMMENT 'Business domain',
    data_category VARCHAR(64) NOT NULL COMMENT 'Data category',
    retention_days INT UNSIGNED NOT NULL COMMENT 'Retention period in days',
    deletion_action VARCHAR(32) NOT NULL COMMENT 'Deletion action after retention',
    legal_basis VARCHAR(64) NOT NULL COMMENT 'Legal basis for retention',
    status VARCHAR(32) NOT NULL COMMENT 'Record status',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_data_retention_policy_code (policy_code),
    KEY idx_data_retention_policy_domain_category (business_domain, data_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Data retention policy definitions';

CREATE TABLE user_consent_record (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    consent_no VARCHAR(64) NOT NULL COMMENT 'Consent record number',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    consent_type VARCHAR(64) NOT NULL COMMENT 'Consent type',
    processing_purpose VARCHAR(128) NOT NULL COMMENT 'Processing purpose code',
    data_categories_json JSON NOT NULL COMMENT 'Consented data category list JSON',
    consent_version VARCHAR(64) NOT NULL COMMENT 'Consent text version',
    consent_channel VARCHAR(64) NOT NULL COMMENT 'Consent capture channel',
    consent_status VARCHAR(32) NOT NULL COMMENT 'Consent status',
    granted_at DATETIME(3) NOT NULL COMMENT 'Consent grant time',
    withdrawn_at DATETIME(3) NULL COMMENT 'Consent withdrawal time',
    evidence_ref VARCHAR(512) NULL COMMENT 'Consent evidence storage reference',
    client_ip_hash CHAR(64) NULL COMMENT 'Client IP hash',
    device_no VARCHAR(128) NULL COMMENT 'Device identifier',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_consent_record_no (consent_no),
    KEY idx_user_consent_record_profile_type (profile_id, consent_type, consent_status),
    KEY idx_user_consent_record_purpose (processing_purpose, granted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User consent and withdrawal records';

CREATE TABLE data_subject_request (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    request_no VARCHAR(64) NOT NULL COMMENT 'Data subject request number',
    profile_id BIGINT UNSIGNED NULL COMMENT 'User profile identifier',
    partner_user_id VARCHAR(64) NULL COMMENT 'Partner user identifier',
    request_type VARCHAR(64) NOT NULL COMMENT 'Request type',
    request_status VARCHAR(32) NOT NULL COMMENT 'Request processing status',
    request_channel VARCHAR(64) NOT NULL COMMENT 'Request channel',
    requested_at DATETIME(3) NOT NULL COMMENT 'Request submission time',
    due_at DATETIME(3) NOT NULL COMMENT 'Required response due time',
    completed_at DATETIME(3) NULL COMMENT 'Request completion time',
    verification_status VARCHAR(32) NOT NULL COMMENT 'Requester verification status',
    verification_ref VARCHAR(512) NULL COMMENT 'Requester verification evidence reference',
    response_ref VARCHAR(512) NULL COMMENT 'Response evidence storage reference',
    reject_reason VARCHAR(512) NULL COMMENT 'Request rejection reason',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_data_subject_request_no (request_no),
    KEY idx_data_subject_request_profile (profile_id, requested_at),
    KEY idx_data_subject_request_status_due (request_status, due_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Data subject access, correction, portability, objection, and erasure requests';

CREATE TABLE credit_application (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    apply_id VARCHAR(64) NOT NULL COMMENT 'Credit application identifier',
    provider_code VARCHAR(32) NOT NULL COMMENT 'External provider code',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    profile_version_id BIGINT UNSIGNED NOT NULL COMMENT 'Profile version identifier',
    external_credit_apply_no VARCHAR(64) NULL COMMENT 'External credit application number',
    status VARCHAR(32) NOT NULL COMMENT 'Record status',
    external_status VARCHAR(32) NULL COMMENT 'External status',
    last_error_code VARCHAR(32) NULL COMMENT 'Last error code',
    submitted_at DATETIME(3) NULL COMMENT 'Submission time',
    finalized_at DATETIME(3) NULL COMMENT 'Final status time',
    next_poll_at DATETIME(3) NULL COMMENT 'Next polling time',
    version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Optimistic lock version',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_credit_application_apply_id (apply_id),
    KEY idx_credit_application_profile_status (profile_id, status),
    KEY idx_credit_application_poll (status, next_poll_at),
    KEY idx_credit_application_external_no (external_credit_apply_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Credit application records';

CREATE TABLE credit_limit_snapshot (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    credit_application_id BIGINT UNSIGNED NOT NULL COMMENT 'Credit application identifier',
    risk_min_limit DECIMAL(18,2) NULL COMMENT 'Minimum available credit limit',
    risk_max_limit DECIMAL(18,2) NULL COMMENT 'Maximum available credit limit',
    psychological_credit_limit DECIMAL(18,2) NULL COMMENT 'Psychological credit limit',
    fake_credit_limit DECIMAL(18,2) NULL COMMENT 'Displayed fake credit limit',
    borrow_amt_step_size DECIMAL(18,2) NULL COMMENT 'Borrow amount step size',
    contract_expire_at DATETIME(3) NULL COMMENT 'Credit contract expiration time',
    source VARCHAR(32) NOT NULL COMMENT 'Record source',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_credit_limit_snapshot_credit_application (credit_application_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Credit limit snapshot records';

CREATE TABLE credit_status_history (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    credit_application_id BIGINT UNSIGNED NOT NULL COMMENT 'Credit application identifier',
    from_status VARCHAR(32) NULL COMMENT 'Previous status',
    to_status VARCHAR(32) NOT NULL COMMENT 'Next status',
    external_status VARCHAR(32) NULL COMMENT 'External status',
    reason_code VARCHAR(64) NULL COMMENT 'Reason or error code',
    source VARCHAR(32) NOT NULL COMMENT 'Record source',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_credit_status_history_credit_created (credit_application_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Credit status transition history';

CREATE TABLE pk_product_snapshot (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    snapshot_no VARCHAR(64) NOT NULL COMMENT 'Snapshot number',
    apply_id VARCHAR(64) NOT NULL COMMENT 'Credit application identifier',
    credit_application_id BIGINT UNSIGNED NOT NULL COMMENT 'Credit application identifier',
    credit_status VARCHAR(32) NOT NULL COMMENT 'Credit status',
    product_status VARCHAR(32) NOT NULL COMMENT 'Product status',
    products_json JSON NOT NULL COMMENT 'Product list JSON',
    fetched_at DATETIME(3) NOT NULL COMMENT 'Fetch time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_product_snapshot_no (snapshot_no),
    KEY idx_pk_product_snapshot_credit_fetched (credit_application_id, fetched_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='PK product list snapshots';

CREATE TABLE loan_quote (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    quote_no VARCHAR(64) NOT NULL COMMENT 'Quote number',
    credit_application_id BIGINT UNSIGNED NOT NULL COMMENT 'Credit application identifier',
    product_snapshot_id BIGINT UNSIGNED NULL COMMENT 'Product snapshot identifier',
    product_code VARCHAR(64) NOT NULL COMMENT 'Product code',
    repay_method VARCHAR(64) NOT NULL COMMENT 'Repayment method code',
    apply_amt DECIMAL(18,2) NOT NULL COMMENT 'Application amount',
    loan_principal DECIMAL(18,2) NULL COMMENT 'Loan principal amount',
    pay_amount DECIMAL(18,2) NULL COMMENT 'Disbursement amount',
    schd_amount DECIMAL(18,2) NULL COMMENT 'Scheduled amount',
    interest DECIMAL(18,2) NULL COMMENT 'Interest amount',
    total_days INT NULL COMMENT 'Total loan days',
    fee_json JSON NULL COMMENT 'Fee detail JSON',
    raw_response_json JSON NOT NULL COMMENT 'Raw response JSON',
    quoted_at DATETIME(3) NOT NULL COMMENT 'Quote time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_loan_quote_no (quote_no),
    KEY idx_loan_quote_credit_quoted (credit_application_id, quoted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Loan quote snapshots';

CREATE TABLE loan_quote_term (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    quote_id BIGINT UNSIGNED NOT NULL COMMENT 'Quote identifier',
    term_no INT NOT NULL COMMENT 'Term number',
    due_date DATETIME(3) NULL COMMENT 'Due date',
    schd_amount DECIMAL(18,2) NULL COMMENT 'Scheduled amount',
    schd_principal DECIMAL(18,2) NULL COMMENT 'Scheduled principal amount',
    schd_interest DECIMAL(18,2) NULL COMMENT 'Scheduled interest amount',
    fee_json JSON NULL COMMENT 'Fee detail JSON',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_loan_quote_term (quote_id, term_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Loan quote repayment term details';

CREATE TABLE loan_application (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    loan_apply_id VARCHAR(64) NOT NULL COMMENT 'Loan application identifier',
    credit_application_id BIGINT UNSIGNED NOT NULL COMMENT 'Credit application identifier',
    quote_id BIGINT UNSIGNED NOT NULL COMMENT 'Quote identifier',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    profile_version_id BIGINT UNSIGNED NOT NULL COMMENT 'Profile version identifier',
    external_loan_apply_no VARCHAR(64) NULL COMMENT 'External loan application number',
    bill_no VARCHAR(64) NULL COMMENT 'Bill number',
    status VARCHAR(32) NOT NULL COMMENT 'Record status',
    external_status VARCHAR(32) NULL COMMENT 'External status',
    apply_amt DECIMAL(18,2) NOT NULL COMMENT 'Application amount',
    pay_amount DECIMAL(18,2) NULL COMMENT 'Disbursement amount',
    pay_time DATETIME(3) NULL COMMENT 'Disbursement time',
    loan_purpose VARCHAR(256) NULL COMMENT 'Loan purpose',
    submitted_at DATETIME(3) NULL COMMENT 'Submission time',
    finalized_at DATETIME(3) NULL COMMENT 'Final status time',
    next_poll_at DATETIME(3) NULL COMMENT 'Next polling time',
    version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Optimistic lock version',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_loan_application_apply_id (loan_apply_id),
    KEY idx_loan_application_profile_status (profile_id, status),
    KEY idx_loan_application_bill_no (bill_no),
    KEY idx_loan_application_poll (status, next_poll_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Loan application records';

CREATE TABLE loan_status_history (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    loan_application_id BIGINT UNSIGNED NOT NULL COMMENT 'Loan application identifier',
    from_status VARCHAR(32) NULL COMMENT 'Previous status',
    to_status VARCHAR(32) NOT NULL COMMENT 'Next status',
    external_status VARCHAR(32) NULL COMMENT 'External status',
    reason_code VARCHAR(64) NULL COMMENT 'Reason or error code',
    source VARCHAR(32) NOT NULL COMMENT 'Record source',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_loan_status_history_loan_created (loan_application_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Loan status transition history';

CREATE TABLE contract_file (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    loan_application_id BIGINT UNSIGNED NOT NULL COMMENT 'Loan application identifier',
    bill_no VARCHAR(64) NULL COMMENT 'Bill number',
    contract_type VARCHAR(64) NOT NULL COMMENT 'Contract type',
    contract_name VARCHAR(128) NOT NULL COMMENT 'Contract name',
    contract_url VARCHAR(1024) NOT NULL COMMENT 'Contract URL',
    file_ref VARCHAR(512) NULL COMMENT 'Object storage file reference',
    fetched_at DATETIME(3) NOT NULL COMMENT 'Fetch time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_contract_file_loan_type (loan_application_id, contract_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Loan contract file references';

CREATE TABLE repayment_plan_term (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    loan_application_id BIGINT UNSIGNED NOT NULL COMMENT 'Loan application identifier',
    loan_apply_id VARCHAR(64) NOT NULL COMMENT 'Loan application identifier',
    bill_no VARCHAR(64) NULL COMMENT 'Bill number',
    sub_bill_no VARCHAR(64) NULL COMMENT 'Sub bill number',
    term_no INT NOT NULL COMMENT 'Term number',
    term_status VARCHAR(32) NOT NULL COMMENT 'Repayment term status',
    due_date DATETIME(3) NULL COMMENT 'Due date',
    grace_date DATETIME(3) NULL COMMENT 'Grace date',
    schd_amount DECIMAL(18,2) NULL COMMENT 'Scheduled amount',
    should_amount DECIMAL(18,2) NULL COMMENT 'Amount due',
    paid_amount DECIMAL(18,2) NULL COMMENT 'Paid amount',
    overdue_days INT NULL COMMENT 'Overdue days',
    amount_detail_json JSON NULL COMMENT 'Amount detail JSON',
    last_repay_time DATETIME(3) NULL COMMENT 'Last repayment time',
    synced_at DATETIME(3) NOT NULL COMMENT 'Last synchronization time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_repayment_plan_term_loan_term (loan_application_id, term_no),
    KEY idx_repayment_plan_term_due_status (due_date, term_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Repayment plan term records';

CREATE TABLE repay_va_snapshot (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    snapshot_no VARCHAR(64) NOT NULL COMMENT 'Snapshot number',
    va_no VARCHAR(128) NOT NULL COMMENT 'Virtual account number',
    bank_code VARCHAR(64) NOT NULL COMMENT 'Bank code',
    bank_name VARCHAR(128) NOT NULL COMMENT 'Bank name',
    default_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Default flag',
    disabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Disabled flag',
    bank_channels_json JSON NULL COMMENT 'Bank channel instruction JSON',
    fetched_at DATETIME(3) NOT NULL COMMENT 'Fetch time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_repay_va_snapshot_profile_fetched (profile_id, fetched_at),
    KEY idx_repay_va_snapshot_va_no (va_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Repayment virtual account snapshots';

CREATE TABLE repayment_trial_snapshot (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    trial_no VARCHAR(64) NOT NULL COMMENT 'Repayment trial number',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    trial_type VARCHAR(32) NOT NULL COMMENT 'Repayment trial type',
    total_bill_count INT UNSIGNED NULL COMMENT 'Total bill count',
    total_should_amount DECIMAL(18,2) NULL COMMENT 'Total amount due',
    total_reduction_amount DECIMAL(18,2) NULL COMMENT 'Total reduction amount',
    default_va_json JSON NULL COMMENT 'Default VA JSON',
    raw_response_json JSON NOT NULL COMMENT 'Raw response JSON',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_repayment_trial_snapshot_no (trial_no),
    KEY idx_repayment_trial_snapshot_profile_created (profile_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Repayment trial snapshots';

CREATE TABLE repayment_trial_order (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    trial_id BIGINT UNSIGNED NOT NULL COMMENT 'Repayment trial identifier',
    loan_application_id BIGINT UNSIGNED NOT NULL COMMENT 'Loan application identifier',
    loan_apply_id VARCHAR(64) NOT NULL COMMENT 'Loan application identifier',
    settle TINYINT(1) NOT NULL COMMENT 'Full settlement flag',
    term_nos_json JSON NULL COMMENT 'Selected term numbers JSON',
    should_amount DECIMAL(18,2) NULL COMMENT 'Amount due',
    bill_status VARCHAR(32) NULL COMMENT 'Bill status',
    term_info_json JSON NULL COMMENT 'Term information JSON',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_repayment_trial_order_trial (trial_id),
    KEY idx_repayment_trial_order_loan_apply (loan_apply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Repayment trial order details';

CREATE TABLE repay_current_order (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    current_order_no VARCHAR(64) NOT NULL COMMENT 'Current repayment order number',
    trial_id BIGINT UNSIGNED NOT NULL COMMENT 'Repayment trial identifier',
    repay_orders_json JSON NOT NULL COMMENT 'Repayment order selection JSON',
    coupon_id BIGINT NULL COMMENT 'Coupon identifier',
    status VARCHAR(32) NOT NULL COMMENT 'Record status',
    submitted_at DATETIME(3) NULL COMMENT 'Submission time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_repay_current_order_no (current_order_no),
    KEY idx_repay_current_order_profile_status (profile_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Current repayment order selections';

CREATE TABLE external_interaction (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    provider_code VARCHAR(32) NOT NULL COMMENT 'External provider code',
    interaction_no VARCHAR(64) NOT NULL COMMENT 'External interaction number',
    business_type VARCHAR(64) NOT NULL COMMENT 'Business type',
    business_id VARCHAR(64) NULL COMMENT 'Business identifier',
    http_method VARCHAR(16) NOT NULL COMMENT 'HTTP method',
    endpoint VARCHAR(256) NOT NULL COMMENT 'API endpoint',
    request_id VARCHAR(64) NULL COMMENT 'Request identifier',
    request_hash CHAR(64) NULL COMMENT 'Request body hash',
    request_ref VARCHAR(512) NULL COMMENT 'Request payload storage reference',
    response_code VARCHAR(32) NULL COMMENT 'External response code',
    response_msg VARCHAR(512) NULL COMMENT 'External response message',
    response_ref VARCHAR(512) NULL COMMENT 'Response payload storage reference',
    success TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Success flag',
    duration_ms INT UNSIGNED NULL COMMENT 'Duration in milliseconds',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_external_interaction_no (interaction_no),
    KEY idx_external_interaction_business (business_type, business_id),
    KEY idx_external_interaction_request_id (request_id),
    KEY idx_external_interaction_endpoint_created (endpoint, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='External API interaction audit records';

CREATE TABLE callback_event (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    callback_no VARCHAR(64) NOT NULL COMMENT 'Callback event number',
    provider_code VARCHAR(32) NOT NULL COMMENT 'External provider code',
    callback_type VARCHAR(64) NOT NULL COMMENT 'Callback type',
    business_id VARCHAR(64) NULL COMMENT 'Business identifier',
    idempotency_key VARCHAR(128) NOT NULL COMMENT 'Idempotency key',
    external_status VARCHAR(32) NULL COMMENT 'External status',
    payload_json JSON NOT NULL COMMENT 'Payload JSON',
    process_status VARCHAR(32) NOT NULL COMMENT 'Processing status',
    received_at DATETIME(3) NOT NULL COMMENT 'Callback received time',
    processed_at DATETIME(3) NULL COMMENT 'Callback processed time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_callback_event_no (callback_no),
    UNIQUE KEY uk_callback_event_idempotency (idempotency_key),
    KEY idx_callback_event_business (callback_type, business_id),
    KEY idx_callback_event_process_status (process_status, received_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='External callback event records';

CREATE TABLE outbox_event (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    event_no VARCHAR(64) NOT NULL COMMENT 'Outbox event number',
    event_type VARCHAR(64) NOT NULL COMMENT 'Event type',
    aggregate_type VARCHAR(64) NOT NULL COMMENT 'Aggregate type',
    aggregate_id VARCHAR(64) NOT NULL COMMENT 'Aggregate identifier',
    payload_json JSON NOT NULL COMMENT 'Payload JSON',
    status VARCHAR(32) NOT NULL COMMENT 'Record status',
    retry_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Retry count',
    next_retry_at DATETIME(3) NULL COMMENT 'Next Retry At',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_outbox_event_no (event_no),
    KEY idx_outbox_event_ready (status, next_retry_at),
    KEY idx_outbox_event_aggregate (aggregate_type, aggregate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Reliable outbox event records';

CREATE TABLE job_execution (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    job_no VARCHAR(64) NOT NULL COMMENT 'Job number',
    event_no VARCHAR(64) NULL COMMENT 'Outbox event number',
    job_type VARCHAR(64) NOT NULL COMMENT 'Job type',
    business_id VARCHAR(64) NULL COMMENT 'Business identifier',
    status VARCHAR(32) NOT NULL COMMENT 'Record status',
    attempt_no INT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Attempt number',
    started_at DATETIME(3) NULL COMMENT 'Start time',
    finished_at DATETIME(3) NULL COMMENT 'Finish time',
    error_code VARCHAR(64) NULL COMMENT 'Error code',
    error_message VARCHAR(1024) NULL COMMENT 'Error message',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_job_execution_no (job_no),
    KEY idx_job_execution_business (job_type, business_id),
    KEY idx_job_execution_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Background job execution records';

CREATE TABLE dead_letter_task (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    task_no VARCHAR(64) NOT NULL COMMENT 'Dead letter task number',
    source_event_no VARCHAR(64) NULL COMMENT 'Source event number',
    job_type VARCHAR(64) NOT NULL COMMENT 'Job type',
    business_id VARCHAR(64) NULL COMMENT 'Business identifier',
    payload_json JSON NOT NULL COMMENT 'Payload JSON',
    error_summary VARCHAR(1024) NULL COMMENT 'Error summary',
    status VARCHAR(32) NOT NULL COMMENT 'Record status',
    resolved_at DATETIME(3) NULL COMMENT 'Resolution time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dead_letter_task_no (task_no),
    KEY idx_dead_letter_task_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Dead letter task records';

CREATE TABLE tracking_event (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    event_id VARCHAR(128) NOT NULL COMMENT 'Tracking event identifier',
    trace_id VARCHAR(64) NULL COMMENT 'Trace identifier',
    partner_user_id VARCHAR(64) NULL COMMENT 'Partner user identifier',
    profile_id BIGINT UNSIGNED NULL COMMENT 'User profile identifier',
    event_type VARCHAR(128) NOT NULL COMMENT 'Event type',
    event_time DATETIME(3) NOT NULL COMMENT 'Event time',
    url VARCHAR(512) NULL COMMENT 'Page URL',
    device_no VARCHAR(128) NULL COMMENT 'Device identifier',
    extend_json JSON NULL COMMENT 'Extended event JSON',
    source VARCHAR(32) NOT NULL COMMENT 'Record source',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_tracking_event_trace (trace_id, event_time),
    KEY idx_tracking_event_user (partner_user_id, event_time),
    KEY idx_tracking_event_type (event_type, event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Client and server tracking events';

CREATE TABLE recon_job (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    job_no VARCHAR(64) NOT NULL COMMENT 'Job number',
    provider_code VARCHAR(32) NOT NULL COMMENT 'External provider code',
    business_date DATE NOT NULL COMMENT 'Business date',
    recon_type VARCHAR(64) NOT NULL COMMENT 'Reconciliation type',
    status VARCHAR(32) NOT NULL COMMENT 'Record status',
    total_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Total record count',
    diff_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Difference count',
    started_at DATETIME(3) NULL COMMENT 'Start time',
    finished_at DATETIME(3) NULL COMMENT 'Finish time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_recon_job_no (job_no),
    UNIQUE KEY uk_recon_job_provider_type_date (provider_code, recon_type, business_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Reconciliation job records';

CREATE TABLE recon_diff (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    recon_job_id BIGINT UNSIGNED NOT NULL COMMENT 'Reconciliation job identifier',
    business_type VARCHAR(64) NOT NULL COMMENT 'Business type',
    business_id VARCHAR(64) NOT NULL COMMENT 'Business identifier',
    diff_type VARCHAR(64) NOT NULL COMMENT 'Difference type',
    local_value_json JSON NULL COMMENT 'Local value JSON',
    external_value_json JSON NULL COMMENT 'External value JSON',
    status VARCHAR(32) NOT NULL COMMENT 'Record status',
    resolved_at DATETIME(3) NULL COMMENT 'Resolution time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_recon_diff_status_created (status, created_at),
    KEY idx_recon_diff_business (business_type, business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Reconciliation difference records';

CREATE TABLE operator_audit_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    operator_id VARCHAR(64) NOT NULL COMMENT 'Operator identifier',
    operation_type VARCHAR(64) NOT NULL COMMENT 'Operation type',
    target_type VARCHAR(64) NOT NULL COMMENT 'Target type',
    target_id VARCHAR(64) NOT NULL COMMENT 'Target identifier',
    before_json JSON NULL COMMENT 'Value before operation JSON',
    after_json JSON NULL COMMENT 'Value after operation JSON',
    reason VARCHAR(512) NULL COMMENT 'Operation reason',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_operator_audit_log_target (target_type, target_id),
    KEY idx_operator_audit_log_operator_created (operator_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Operator audit log records';
