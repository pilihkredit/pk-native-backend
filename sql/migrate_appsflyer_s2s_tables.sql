-- AppsFlyer S2S tables for server event push callback -> AF reporting.
-- Table names reuse credit-core adjust_* naming for shared App ID / Dev Key.

CREATE TABLE IF NOT EXISTS adjust_config (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    app_token VARCHAR(128) NOT NULL COMMENT 'AF App ID',
    api_token VARCHAR(256) NOT NULL COMMENT 'AF Dev Key (authentication header)',
    base_url VARCHAR(512) NOT NULL COMMENT 'AF S2S URL, e.g. https://api2.appsflyer.com/inappevent/{appId}',
    timeout INT UNSIGNED NOT NULL DEFAULT 30000 COMMENT 'HTTP timeout milliseconds',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether config is active',
    os_name VARCHAR(50) NULL COMMENT 'iOS / Android',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_adjust_config_active_os (is_active, os_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AppsFlyer S2S API configs';

CREATE TABLE IF NOT EXISTS adjust_event_config (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    event_name VARCHAR(128) NOT NULL COMMENT 'Lender callback eventType / AF eventName',
    app_token VARCHAR(128) NOT NULL COMMENT 'AF App ID matching adjust_config.app_token',
    description VARCHAR(256) NULL COMMENT 'Description',
    is_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether event reporting is enabled',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_adjust_event_config (event_name, app_token),
    KEY idx_adjust_event_config_enabled (is_enabled, event_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AppsFlyer event enablement config';

CREATE TABLE IF NOT EXISTS adjust_event_record (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    server_event_callback_id BIGINT UNSIGNED NULL COMMENT 'Related lender_server_event_callback.id',
    partner_user_id VARCHAR(64) NULL COMMENT 'Partner user identifier',
    user_id BIGINT UNSIGNED NULL COMMENT 'user_profile.id',
    device_uuid VARCHAR(128) NULL COMMENT 'Device identifier (deviceNo)',
    event_name VARCHAR(128) NOT NULL COMMENT 'AF event name',
    app_token VARCHAR(128) NULL COMMENT 'AF App ID',
    idfa VARCHAR(128) NULL COMMENT 'iOS advertising identifier',
    idfv VARCHAR(128) NULL COMMENT 'iOS vendor identifier',
    gps_adid VARCHAR(128) NULL COMMENT 'Android advertising identifier',
    adid VARCHAR(128) NULL COMMENT 'AppsFlyer ID sent as appsflyer_id',
    event_timestamp BIGINT NOT NULL COMMENT 'Event time epoch millis',
    status INT NOT NULL DEFAULT 0 COMMENT '0=pending,1=success,2=failed',
    response TEXT NULL COMMENT 'AF response body',
    error_message VARCHAR(1024) NULL COMMENT 'Error message',
    retry_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Retry count',
    extra_params JSON NULL COMMENT 'Original callback payload snapshot',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_adjust_event_record_server_event (server_event_callback_id),
    KEY idx_adjust_event_record_partner_event (partner_user_id, event_name),
    KEY idx_adjust_event_record_status (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AppsFlyer S2S report records';
