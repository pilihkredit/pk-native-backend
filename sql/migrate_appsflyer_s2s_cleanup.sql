-- Cleanup legacy AF migration artifacts (run only if old migrate_appsflyer_s2s_tables.sql was applied).

ALTER TABLE adjust_event_config DROP COLUMN IF EXISTS event_token;
ALTER TABLE adjust_event_record DROP COLUMN IF EXISTS event_token;
DROP TABLE IF EXISTS app_conf;
