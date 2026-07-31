-- SMS OTP gateway config aligned with pk-credit-core smsConf.
-- Fill url / spid / pwd with production gateway credentials per environment.
-- Safe to re-run.

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
    `value` = JSON_SET(
        COALESCE(`value`, JSON_OBJECT()),
        '$.enableSms', true,
        '$.contentTemplate', COALESCE(
            JSON_UNQUOTE(JSON_EXTRACT(`value`, '$.contentTemplate')),
            '[PilihKredit] Kode verifikasi Anda adalah {code} valid selama {minutes} menit. JANGAN Bagikan kode ini kepada siapapun!'
        ),
        '$.commercialCode', COALESCE(
            NULLIF(JSON_UNQUOTE(JSON_EXTRACT(`value`, '$.commercialCode')), ''),
            '0062'
        ),
        '$.expireTime', COALESCE(
            CAST(JSON_UNQUOTE(JSON_EXTRACT(`value`, '$.expireTime')) AS UNSIGNED),
            300
        ),
        '$.timeout', COALESCE(
            CAST(JSON_UNQUOTE(JSON_EXTRACT(`value`, '$.timeout')) AS UNSIGNED),
            10000
        )
    );
