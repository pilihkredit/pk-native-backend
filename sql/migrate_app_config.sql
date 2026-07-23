-- Application config key-value store (value is JSON).
-- Run once per environment.

CREATE TABLE IF NOT EXISTS app_config (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `key` VARCHAR(128) NOT NULL COMMENT 'Config key',
    `value` JSON NOT NULL COMMENT 'Config value JSON',
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_config_key (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Application configuration key-value store';

INSERT INTO app_config (`key`, `value`)
VALUES (
    'auth.otp',
    JSON_OBJECT(
        'otpDailyLimit', 10,
        'otpResendIntervalSeconds', 60,
        'otpDailyLimitZone', 'Asia/Jakarta'
    )
)
ON DUPLICATE KEY UPDATE
    `value` = VALUES(`value`);

INSERT INTO app_config (`key`, `value`)
VALUES (
    'whatsappConf',
    JSON_OBJECT(
        'enableWhatsApp', false,
        'url', 'https://api.innopaas.com/api/whatsapp/v3',
        'appKey', '',
        'authorization', '',
        'wabaId', '',
        'sendNumber', '',
        'templateName', 'otp_pilihkredit',
        'language', 'id',
        'countryDialCode', '62',
        'timeout', 10000,
        'minInterval', 60,
        'expireTime', 300
    )
)
ON DUPLICATE KEY UPDATE
    `value` = VALUES(`value`);

INSERT INTO app_config (`key`, `value`)
VALUES (
    'whatsapp_daily_limit',
    CAST('5' AS JSON)
)
ON DUPLICATE KEY UPDATE
    `value` = VALUES(`value`);

INSERT INTO app_config (`key`, `value`)
VALUES (
    'bank_card_max_count',
    CAST('5' AS JSON)
)
ON DUPLICATE KEY UPDATE
    `value` = VALUES(`value`);
