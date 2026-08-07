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
        'templateGroupId', '',
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
    'smsConf',
    JSON_OBJECT(
        'enableSms', true,
        'url', '',
        'spid', '',
        'pwd', '',
        'commercialCode', '0062',
        'contentTemplate', '[PilihKredit] Kode verifikasi Anda adalah {code} valid selama {minutes} menit. JANGAN Bagikan kode ini kepada siapapun!',
        'expireTime', 300,
        'minInterval', 60,
        'codeLength', 6,
        'numericOnly', true,
        'timeout', 10000,
        'defaultCode', '1234',
        'userList', JSON_ARRAY()
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
