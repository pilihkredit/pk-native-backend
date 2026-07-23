-- Unified inbound callback audit log (similar to external_interaction).

SET @schema_name = DATABASE();

SET @table_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'external_interaction_callback'
);
SET @ddl = IF(
    @table_exists = 0,
    'CREATE TABLE external_interaction_callback (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT ''Primary key'',
        provider_code VARCHAR(32) NOT NULL COMMENT ''External provider code'',
        interaction_no VARCHAR(64) NOT NULL COMMENT ''Callback interaction number'',
        idempotency_key VARCHAR(128) NOT NULL COMMENT ''Callback idempotency key'',
        business_type VARCHAR(64) NOT NULL COMMENT ''Callback business type'',
        business_id VARCHAR(64) NULL COMMENT ''Business identifier'',
        mobile_no VARCHAR(32) NULL COMMENT ''Account owner mobile number'',
        http_method VARCHAR(16) NOT NULL COMMENT ''HTTP method'',
        endpoint VARCHAR(256) NOT NULL COMMENT ''API endpoint'',
        request_id VARCHAR(64) NULL COMMENT ''Request identifier'',
        request_hash CHAR(64) NULL COMMENT ''Request body hash'',
        request_ref MEDIUMTEXT NULL COMMENT ''Request payload storage reference'',
        response_code VARCHAR(32) NULL COMMENT ''Our HTTP response code'',
        response_msg VARCHAR(512) NULL COMMENT ''Our HTTP response message'',
        response_ref MEDIUMTEXT NULL COMMENT ''Our HTTP response payload storage reference'',
        success TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''Processing success flag'',
        duration_ms INT UNSIGNED NULL COMMENT ''Duration in milliseconds'',
        created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT ''Record creation time'',
        updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT ''Record update time'',
        PRIMARY KEY (id),
        UNIQUE KEY uk_external_interaction_callback_no (interaction_no),
        UNIQUE KEY uk_external_interaction_callback_idempotency (idempotency_key),
        KEY idx_external_interaction_callback_business (business_type, business_id),
        KEY idx_external_interaction_callback_endpoint_created (endpoint, created_at),
        KEY idx_external_interaction_callback_mobile_created (mobile_no, created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT=''Inbound callback request/response audit records''',
    'SELECT ''skip: external_interaction_callback exists'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
