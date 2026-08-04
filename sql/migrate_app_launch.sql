-- Application cold launch events and daily aggregates.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

CREATE TABLE IF NOT EXISTS app_launch_event (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    launch_id VARCHAR(64) NOT NULL COMMENT 'Client-generated idempotency key',
    device_no VARCHAR(128) NOT NULL COMMENT 'Client device identifier',
    user_id BIGINT UNSIGNED NULL COMMENT 'user_profile.id when authenticated',
    app_version VARCHAR(64) NOT NULL COMMENT 'Client application version',
    platform VARCHAR(32) NOT NULL COMMENT 'Client platform',
    app_package VARCHAR(255) NOT NULL COMMENT 'Client application package',
    client_started_at DATETIME(3) NULL COMMENT 'Client-reported cold start time',
    occurred_at DATETIME(3) NOT NULL COMMENT 'Server-recorded launch time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_launch_event_launch_id (launch_id),
    KEY idx_app_launch_event_time (occurred_at),
    KEY idx_app_launch_event_device_time (device_no, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Raw application cold launch events';
