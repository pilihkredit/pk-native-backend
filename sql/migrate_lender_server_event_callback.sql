-- Replace generic callback_event JSON storage for /callback/event/push
-- with a structured table matching lender OpenAPI fields.
-- AF S2S records retarget server_event_callback_id.

CREATE TABLE IF NOT EXISTS lender_server_event_callback (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    event_id VARCHAR(128) NOT NULL COMMENT 'Lender event ID',
    event_type VARCHAR(128) NOT NULL COMMENT 'Tracking event name',
    event_time BIGINT NOT NULL COMMENT 'Event time epoch millis (13-digit)',
    event_value VARCHAR(1024) NULL COMMENT 'Event additional value',
    value DECIMAL(20, 4) NULL COMMENT 'Event amount / numeric value',
    client_id VARCHAR(128) NULL COMMENT 'Open client ID',
    user_id VARCHAR(128) NULL COMMENT 'Lender platform user ID',
    partner_user_id VARCHAR(64) NULL COMMENT 'Partner user ID',
    app_name VARCHAR(128) NULL COMMENT 'App name',
    country_code VARCHAR(16) NULL COMMENT 'Country code',
    app_version VARCHAR(64) NULL COMMENT 'App version',
    country_name VARCHAR(128) NULL COMMENT 'Country name',
    device_no VARCHAR(128) NULL COMMENT 'Device number',
    system_platform VARCHAR(64) NULL COMMENT 'System platform',
    ad_id VARCHAR(128) NULL COMMENT 'Advertising ID',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_lender_server_event_callback_event_id (event_id),
    KEY idx_lender_server_event_callback_partner_type (partner_user_id, event_type),
    KEY idx_lender_server_event_callback_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Lender server event push callbacks (structured fields)';

-- Retarget AF report rows from callback_event to lender_server_event_callback.
ALTER TABLE adjust_event_record
    CHANGE COLUMN callback_event_id server_event_callback_id BIGINT UNSIGNED NULL
        COMMENT 'Related lender_server_event_callback.id';

DROP TABLE IF EXISTS callback_event;
