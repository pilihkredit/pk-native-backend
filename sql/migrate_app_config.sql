-- Application config key-value store (value is JSON).
-- Run once per environment.

CREATE TABLE IF NOT EXISTS app_config (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `key` VARCHAR(128) NOT NULL COMMENT 'Config key',
    `value` JSON NOT NULL COMMENT 'Config value JSON',
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_config_key (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Application configuration key-value store';
