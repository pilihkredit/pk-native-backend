-- AppsFlyer S2S config + report tables (reuse credit-core adjust_* naming).
-- Shared App ID / Dev Key strategy with pk-credit-core.

CREATE TABLE IF NOT EXISTS app_conf (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    config_key VARCHAR(64) NOT NULL COMMENT 'Config key',
    config_value TEXT NOT NULL COMMENT 'Config JSON/text value',
    description VARCHAR(256) NULL COMMENT 'Description',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_conf_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Application runtime configuration';

CREATE TABLE IF NOT EXISTS adjust_config (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    app_token VARCHAR(128) NOT NULL COMMENT 'AF App ID (reused from credit-core naming)',
    api_token VARCHAR(256) NOT NULL COMMENT 'AF Dev Key used as authentication header',
    base_url VARCHAR(512) NOT NULL COMMENT 'AF S2S base URL, e.g. https://api2.appsflyer.com/inappevent/{appId}',
    timeout INT UNSIGNED NOT NULL DEFAULT 30000 COMMENT 'HTTP timeout milliseconds',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether config is active',
    os_name VARCHAR(50) NULL COMMENT 'Operating system name: iOS / Android',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_adjust_config_active_os (is_active, os_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AF/Adjust S2S API configs';

CREATE TABLE IF NOT EXISTS adjust_event_config (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    event_name VARCHAR(128) NOT NULL COMMENT 'Business event name / lender eventType',
    event_token VARCHAR(128) NULL COMMENT 'Optional event token',
    app_token VARCHAR(128) NOT NULL COMMENT 'AF App ID matching adjust_config.app_token',
    description VARCHAR(256) NULL COMMENT 'Description',
    is_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether event reporting is enabled',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_adjust_event_config (event_name, app_token),
    KEY idx_adjust_event_config_enabled (is_enabled, event_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AF/Adjust event enablement config';

CREATE TABLE IF NOT EXISTS adjust_event_record (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    callback_event_id BIGINT UNSIGNED NULL COMMENT 'Related callback_event.id',
    partner_user_id VARCHAR(64) NULL COMMENT 'Partner user identifier',
    profile_id BIGINT UNSIGNED NULL COMMENT 'User profile identifier',
    device_uuid VARCHAR(128) NULL COMMENT 'Device identifier',
    event_name VARCHAR(128) NOT NULL COMMENT 'AF event name',
    event_token VARCHAR(128) NULL COMMENT 'Event token',
    app_token VARCHAR(128) NULL COMMENT 'AF App ID',
    idfa VARCHAR(128) NULL COMMENT 'iOS advertising identifier',
    idfv VARCHAR(128) NULL COMMENT 'iOS vendor identifier',
    gps_adid VARCHAR(128) NULL COMMENT 'Android advertising identifier',
    adid VARCHAR(128) NULL COMMENT 'AppsFlyer ID used as appsflyer_id',
    event_timestamp BIGINT NOT NULL COMMENT 'Event time epoch millis',
    status INT NOT NULL DEFAULT 0 COMMENT '0=pending,1=success,2=failed',
    response TEXT NULL COMMENT 'AF response body',
    error_message VARCHAR(1024) NULL COMMENT 'Error message',
    retry_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Retry count',
    extra_params JSON NULL COMMENT 'Extra event payload',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_adjust_event_record_callback (callback_event_id),
    KEY idx_adjust_event_record_partner_event (partner_user_id, event_name),
    KEY idx_adjust_event_record_status (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AF/Adjust S2S report records';

INSERT INTO app_conf (config_key, config_value, description)
SELECT 'reportEvent', '{"reportSource":"af"}', 'Event reporting source: af or adjust'
WHERE NOT EXISTS (SELECT 1 FROM app_conf WHERE config_key = 'reportEvent');
