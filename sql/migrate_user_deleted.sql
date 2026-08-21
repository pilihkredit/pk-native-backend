-- Account deletion queue for retention mark (Job A) and physical purge (Job B).
-- Apply manually on existing DBs. Also embedded in create_pk_schema.sql for fresh installs.

CREATE TABLE IF NOT EXISTS user_deleted (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'user_profile.id',
    partner_user_id VARCHAR(64) NULL COMMENT 'Snapshot of partner_user_id at delete request',
    mobile_no VARCHAR(32) NULL COMMENT 'Snapshot of mobile_no at delete request',
    reason VARCHAR(128) NULL COMMENT 'Close/delete reason',
    requested_at DATETIME(3) NOT NULL COMMENT 'User requested close/delete time',
    status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending|marked|purged|failed',
    retention_until DATETIME(3) NULL COMMENT 'Copied from user_profile after mark job',
    marked_at DATETIME(3) NULL COMMENT 'Mark job success time',
    purged_at DATETIME(3) NULL COMMENT 'Purge job success time',
    fail_reason VARCHAR(512) NULL COMMENT 'Last failure message',
    retry_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Retry times',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_deleted_user_id (user_id),
    KEY idx_user_deleted_status_id (status, id),
    KEY idx_user_deleted_retention_until (retention_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Account deletion queue for retention mark and physical purge';
