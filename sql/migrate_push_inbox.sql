-- Push device registration and inbox message records.
-- Compatible with MySQL 5.7 / 8.0. Safe to run once on existing environments.

CREATE TABLE IF NOT EXISTS push_device (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT UNSIGNED NULL COMMENT 'user_profile.id when authenticated',
    device_no VARCHAR(128) NOT NULL COMMENT 'Client device identifier',
    app_version VARCHAR(64) NOT NULL COMMENT 'Client application version',
    platform VARCHAR(32) NOT NULL COMMENT 'Client platform',
    app_package VARCHAR(255) NOT NULL COMMENT 'Client application package',
    fcm_token VARCHAR(512) NOT NULL COMMENT 'Firebase Cloud Messaging registration token',
    permission_status VARCHAR(32) NOT NULL COMMENT 'GRANTED, DENIED, or NOT_DETERMINED',
    bound_at DATETIME(3) NULL COMMENT 'Latest authenticated user binding time',
    unbound_at DATETIME(3) NULL COMMENT 'Latest authenticated user unbinding time',
    last_seen_at DATETIME(3) NOT NULL COMMENT 'Latest device registration time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_push_device_fcm_token (fcm_token),
    UNIQUE KEY uk_push_device_device_package (device_no, app_package),
    KEY idx_push_device_user_permission (user_id, permission_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FCM device registration records';

CREATE TABLE IF NOT EXISTS inbox_message (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    type VARCHAR(64) NOT NULL COMMENT 'Message business type',
    title VARCHAR(256) NOT NULL COMMENT 'Message title',
    summary VARCHAR(512) NULL COMMENT 'Message list summary',
    content TEXT NOT NULL COMMENT 'Message detail content',
    deeplink VARCHAR(1024) NULL COMMENT 'Application deeplink target',
    sent_at DATETIME(3) NOT NULL COMMENT 'Message publish time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_inbox_message_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Inbox message content records';

CREATE TABLE IF NOT EXISTS inbox_user_message (
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'user_profile.id',
    message_id BIGINT UNSIGNED NOT NULL COMMENT 'inbox_message.id',
    delivered_at DATETIME(3) NOT NULL COMMENT 'Message delivery record time',
    read_at DATETIME(3) NULL COMMENT 'Message first read time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'Record update time',
    PRIMARY KEY (user_id, message_id),
    KEY idx_inbox_user_message_unread (user_id, read_at, message_id),
    KEY idx_inbox_user_message_message (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Per-user inbox delivery and read state records';
