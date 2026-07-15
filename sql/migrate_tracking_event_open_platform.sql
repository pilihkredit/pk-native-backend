-- Align tracking_event with Open Platform client analytics payload (flat fields + full payload JSON).
-- MySQL 8+

ALTER TABLE tracking_event
    DROP INDEX idx_tracking_event_trace,
    DROP INDEX idx_tracking_event_user,
    DROP INDEX idx_tracking_event_type;

ALTER TABLE tracking_event
    ADD COLUMN event_timestamp BIGINT NULL COMMENT 'Client event timestamp in milliseconds' AFTER id,
    ADD COLUMN uid VARCHAR(128) NULL COMMENT 'User ID from client or login context' AFTER event_timestamp,
    ADD COLUMN client_no VARCHAR(128) NULL COMMENT 'Device ID' AFTER url,
    ADD COLUMN client_manufacture VARCHAR(64) NULL COMMENT 'Device manufacturer' AFTER client_no,
    ADD COLUMN client_model VARCHAR(128) NULL COMMENT 'Device model' AFTER client_manufacture,
    ADD COLUMN client_category VARCHAR(32) NULL COMMENT 'Device category' AFTER client_model,
    ADD COLUMN client_os VARCHAR(32) NULL COMMENT 'Device OS' AFTER client_category,
    ADD COLUMN client_os_version VARCHAR(64) NULL COMMENT 'Device OS version' AFTER client_os,
    ADD COLUMN ai VARCHAR(128) NULL COMMENT 'App package name' AFTER client_os_version,
    ADD COLUMN av VARCHAR(64) NULL COMMENT 'App version' AFTER ai,
    ADD COLUMN wv VARCHAR(64) NULL COMMENT 'Web version' AFTER av,
    ADD COLUMN bn VARCHAR(64) NULL COMMENT 'Browser name' AFTER wv,
    ADD COLUMN bv VARCHAR(64) NULL COMMENT 'Browser version' AFTER bn,
    ADD COLUMN android_id VARCHAR(128) NULL COMMENT 'Android ID' AFTER bv,
    ADD COLUMN gaid VARCHAR(128) NULL COMMENT 'Google Advertising ID' AFTER android_id,
    ADD COLUMN idfv VARCHAR(128) NULL COMMENT 'iOS identifier for vendor' AFTER gaid,
    ADD COLUMN idfa VARCHAR(128) NULL COMMENT 'iOS advertising identifier' AFTER idfv,
    ADD COLUMN ip VARCHAR(64) NULL COMMENT 'Client IP filled by server' AFTER idfa,
    ADD COLUMN event_datetime VARCHAR(32) NULL COMMENT 'Event datetime filled by server (Asia/Jakarta)' AFTER ip,
    ADD COLUMN payload_json JSON NULL COMMENT 'Full tracking payload as forwarded/saved' AFTER event_datetime;

UPDATE tracking_event
SET event_timestamp = CASE
        WHEN event_time IS NULL THEN NULL
        ELSE ROUND(UNIX_TIMESTAMP(event_time) * 1000)
    END,
    uid = partner_user_id,
    client_no = device_no,
    payload_json = JSON_OBJECT(
        'timestamp', ROUND(UNIX_TIMESTAMP(event_time) * 1000),
        'uid', IFNULL(partner_user_id, ''),
        'eventType', event_type,
        'url', url,
        'extend', CAST(IFNULL(extend_json, JSON_OBJECT()) AS JSON),
        'traceId', trace_id,
        'clientNo', device_no
    );

UPDATE tracking_event
SET event_timestamp = 0
WHERE event_timestamp IS NULL;

UPDATE tracking_event
SET payload_json = JSON_OBJECT()
WHERE payload_json IS NULL;

ALTER TABLE tracking_event
    MODIFY COLUMN event_timestamp BIGINT NOT NULL COMMENT 'Client event timestamp in milliseconds',
    MODIFY COLUMN payload_json JSON NOT NULL COMMENT 'Full tracking payload as forwarded/saved',
    DROP COLUMN event_id,
    DROP COLUMN event_time,
    DROP COLUMN device_no;

ALTER TABLE tracking_event
    ADD KEY idx_tracking_event_trace (trace_id, event_timestamp),
    ADD KEY idx_tracking_event_uid (uid, event_timestamp),
    ADD KEY idx_tracking_event_type (event_type, event_timestamp),
    ADD KEY idx_tracking_event_partner (partner_user_id, event_timestamp);
