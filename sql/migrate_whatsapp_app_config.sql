-- WhatsApp OTP config in app_config (keys align with pk-credit-core app_conf).
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

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
        'expireTime', 300,
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
