-- Push notification tasks published from pk-analytics and delivered by pk-worker.
-- Run against the PK business DB.

CREATE TABLE push_task (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    task_name VARCHAR(128) NOT NULL COMMENT 'Backoffice task label',
    title VARCHAR(256) NOT NULL COMMENT 'Popup and notification title',
    body VARCHAR(1024) NOT NULL COMMENT 'Popup and notification body',
    button_text VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'Popup button label',
    push_type VARCHAR(32) NOT NULL COMMENT 'internal / external / all / clear_required',
    required_read TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Mandatory in-app popup flag',
    push_pages JSON NULL COMMENT 'Allowed in-app page route list, null or empty = any page',
    internal_url VARCHAR(512) NULL COMMENT 'H5 preset popup path for in-app display',
    target_url VARCHAR(512) NULL COMMENT 'Popup button jump target',
    external_url VARCHAR(512) NULL COMMENT 'System notification click target',
    banner_url VARCHAR(512) NULL COMMENT 'Popup banner image path',
    audience_type VARCHAR(16) NOT NULL DEFAULT 'all' COMMENT 'all / upload',
    audience_file_name VARCHAR(256) NULL COMMENT 'Original uploaded audience file name',
    clear_target_ids JSON NULL COMMENT 'push_task ids cleared by a clear_required task',
    notes VARCHAR(512) NULL COMMENT 'Backoffice notes, not shown to users',
    status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED' COMMENT 'PUBLISHED / SENDING / COMPLETED / FAILED',
    status_message VARCHAR(512) NULL COMMENT 'Delivery failure note',
    total_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Resolved recipient device count',
    success_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Successful FCM sends',
    failure_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Failed FCM sends',
    cleared_at DATETIME(3) NULL COMMENT 'Set when superseded by a clear_required push',
    published_at DATETIME(3) NOT NULL COMMENT 'Backoffice publish time',
    send_completed_at DATETIME(3) NULL COMMENT 'Delivery loop finish time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'Update time',
    PRIMARY KEY (id),
    KEY idx_push_task_delivery (status, published_at),
    KEY idx_push_task_pending (required_read, cleared_at, published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Backoffice push notification tasks';

CREATE TABLE push_task_audience (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    task_id BIGINT UNSIGNED NOT NULL COMMENT 'push_task.id',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Uploaded audience mobile number',
    resolved_user_id BIGINT UNSIGNED NULL COMMENT 'user_profile.id when the mobile matches',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'Update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_push_task_audience (task_id, mobile_no),
    KEY idx_push_task_audience_user (resolved_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Uploaded push audience rows copied from staging at task creation';

CREATE TABLE push_audience_upload (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    upload_token VARCHAR(40) NOT NULL COMMENT 'Staging token returned to the backoffice',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Parsed audience mobile number',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'Update time',
    PRIMARY KEY (id),
    KEY idx_push_audience_upload_token (upload_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Staging rows of one parsed audience file upload';

CREATE TABLE push_display_log (
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'user_profile.id',
    task_id BIGINT UNSIGNED NOT NULL COMMENT 'push_task.id',
    displayed_at DATETIME(3) NOT NULL COMMENT 'In-app popup displayed time',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'Update time',
    PRIMARY KEY (user_id, task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Per-user mandatory popup display records';
