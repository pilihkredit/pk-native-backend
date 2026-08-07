-- Replace whatsappConf.templateName with templateGroupId for Chuanglan template groups.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

UPDATE app_config
SET `value` = JSON_SET(
        JSON_REMOVE(`value`, '$.templateName'),
        '$.templateGroupId',
        IFNULL(
                NULLIF(JSON_UNQUOTE(JSON_EXTRACT(`value`, '$.templateGroupId')), 'null'),
                ''
        )
)
WHERE `key` = 'whatsappConf'
  AND (
        JSON_CONTAINS_PATH(`value`, 'one', '$.templateName')
        OR NOT JSON_CONTAINS_PATH(`value`, 'one', '$.templateGroupId')
      );
