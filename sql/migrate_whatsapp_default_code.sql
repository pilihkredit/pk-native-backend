-- Add WhatsApp defaultCode / userList to existing whatsappConf (safe to re-run).
-- Does not overwrite other fields already present.

UPDATE app_config
SET `value` = JSON_SET(
    COALESCE(`value`, JSON_OBJECT()),
    '$.defaultCode', COALESCE(
        NULLIF(JSON_UNQUOTE(JSON_EXTRACT(`value`, '$.defaultCode')), ''),
        '123456'
    ),
    '$.userList', COALESCE(
        JSON_EXTRACT(`value`, '$.userList'),
        JSON_ARRAY()
    )
)
WHERE `key` = 'whatsappConf';
