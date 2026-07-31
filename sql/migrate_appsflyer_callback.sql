-- AppsFlyer cloud Push API webhook storage.
-- Columns cover lender appsFlyerInstall fields for future lookup + raw body.
-- device_no / user_id are partner fields (not in AF Push); backfilled from user_profile_af by appsflyer_id.

CREATE TABLE IF NOT EXISTS appsflyer_callback (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT UNSIGNED NULL COMMENT 'user_profile.id (resolved from local AF install)',
    device_no VARCHAR(64) NULL COMMENT 'Client device number (resolved from local AF install)',
    appsflyer_id VARCHAR(128) NULL COMMENT 'AppsFlyer ID',
    advertising_id VARCHAR(128) NULL COMMENT 'Advertising ID / GAID',
    android_id VARCHAR(128) NULL COMMENT 'Android ID',
    attributed_touch_time VARCHAR(64) NULL COMMENT 'Attributed touch time',
    attributed_touch_time_selected_timezone VARCHAR(64) NULL COMMENT 'AF attributed_touch_time_selected_timezone',
    gp_click_time VARCHAR(64) NULL COMMENT 'Google Play click time',
    install_time VARCHAR(64) NULL COMMENT 'Install time',
    media_source VARCHAR(256) NULL COMMENT 'Media source',
    af_prt VARCHAR(256) NULL COMMENT 'Partner identifier',
    af_adset_id VARCHAR(256) NULL COMMENT 'Adset ID',
    af_adset VARCHAR(256) NULL COMMENT 'Adset name',
    af_siteid VARCHAR(256) NULL COMMENT 'Site ID',
    af_c_id VARCHAR(256) NULL COMMENT 'Campaign ID',
    campaign VARCHAR(512) NULL COMMENT 'Campaign',
    app_version VARCHAR(64) NULL COMMENT 'App version',
    app_id VARCHAR(128) NULL COMMENT 'App ID / package',
    device_type VARCHAR(128) NULL COMMENT 'Device type',
    os_version VARCHAR(64) NULL COMMENT 'OS version',
    country_code VARCHAR(16) NULL COMMENT 'Country code',
    city VARCHAR(128) NULL COMMENT 'City',
    postal_code VARCHAR(32) NULL COMMENT 'Postal code',
    ip VARCHAR(64) NULL COMMENT 'IP',
    operator VARCHAR(128) NULL COMMENT 'Carrier / operator',
    device_category VARCHAR(64) NULL COMMENT 'Device category',
    platform VARCHAR(32) NULL COMMENT 'Platform',
    device_model VARCHAR(128) NULL COMMENT 'Device model',
    idfv VARCHAR(128) NULL COMMENT 'iOS IDFV',
    idfa VARCHAR(128) NULL COMMENT 'iOS IDFA',
    af_ad VARCHAR(256) NULL COMMENT 'Ad name',
    af_channel VARCHAR(256) NULL COMMENT 'AF channel',
    attributed_touch_type VARCHAR(64) NULL COMMENT 'Attributed touch type',
    af_ad_id VARCHAR(256) NULL COMMENT 'AF ad ID',
    af_ad_type VARCHAR(64) NULL COMMENT 'AF ad type',
    contributor1_touch_type VARCHAR(64) NULL COMMENT 'Contributor1 touch type',
    contributor1_touch_time VARCHAR(64) NULL COMMENT 'Contributor1 touch time',
    contributor1_af_prt VARCHAR(256) NULL COMMENT 'Contributor1 partner',
    contributor1_match_type VARCHAR(64) NULL COMMENT 'Contributor1 match type',
    contributor1_engagement_type VARCHAR(64) NULL COMMENT 'Contributor1 engagement type',
    bundle_id VARCHAR(128) NULL COMMENT 'Bundle ID',
    match_type VARCHAR(64) NULL COMMENT 'Match type',
    gp_install_begin VARCHAR(64) NULL COMMENT 'GP install begin time',
    event_source VARCHAR(64) NULL COMMENT 'AF event_source',
    event_time VARCHAR(64) NULL COMMENT 'AF event_time',
    event_time_selected_timezone VARCHAR(64) NULL COMMENT 'AF event_time_selected_timezone',
    app_name VARCHAR(256) NULL COMMENT 'AF app_name',
    app_type VARCHAR(64) NULL COMMENT 'AF app_type',
    campaign_type VARCHAR(64) NULL COMMENT 'AF campaign_type',
    conversion_type VARCHAR(64) NULL COMMENT 'AF conversion_type',
    engagement_type VARCHAR(64) NULL COMMENT 'AF engagement_type',
    af_attribution_lookback VARCHAR(64) NULL COMMENT 'AF af_attribution_lookback',
    is_retargeting TINYINT(1) NULL COMMENT 'AF is_retargeting',
    region VARCHAR(64) NULL COMMENT 'AF region',
    state VARCHAR(64) NULL COMMENT 'AF state',
    dma VARCHAR(64) NULL COMMENT 'AF dma',
    wifi TINYINT(1) NULL COMMENT 'AF wifi',
    carrier VARCHAR(128) NULL COMMENT 'AF carrier',
    language VARCHAR(64) NULL COMMENT 'AF language',
    install_time_selected_timezone VARCHAR(64) NULL COMMENT 'AF install_time_selected_timezone',
    device_download_time VARCHAR(64) NULL COMMENT 'AF device_download_time',
    device_download_time_selected_timezone VARCHAR(64) NULL COMMENT 'AF device_download_time_selected_timezone',
    gp_referrer VARCHAR(1024) NULL COMMENT 'AF gp_referrer',
    sdk_version VARCHAR(64) NULL COMMENT 'AF sdk_version',
    api_version VARCHAR(32) NULL COMMENT 'AF api_version',
    user_agent VARCHAR(512) NULL COMMENT 'AF user_agent',
    selected_timezone VARCHAR(64) NULL COMMENT 'AF selected_timezone',
    selected_currency VARCHAR(16) NULL COMMENT 'AF selected_currency',
    is_lat TINYINT(1) NULL COMMENT 'AF is_lat',
    att VARCHAR(64) NULL COMMENT 'AF att',
    original_url VARCHAR(1024) NULL COMMENT 'AF original_url',
    http_referrer VARCHAR(1024) NULL COMMENT 'AF http_referrer',
    event_value TEXT NULL COMMENT 'AF event_value JSON/string',
    event_value_app_id VARCHAR(128) NULL COMMENT 'app_id extracted from event_value',
    event_name VARCHAR(128) NULL COMMENT 'AF event_name',
    event_type VARCHAR(128) NULL COMMENT 'AF event_type',
    customer_user_id VARCHAR(128) NULL COMMENT 'AF customer_user_id',
    raw_data LONGTEXT NOT NULL COMMENT 'Full AppsFlyer Push JSON body',
    callback_status VARCHAR(32) NOT NULL DEFAULT 'processed' COMMENT 'received|processed|failed',
    error_message VARCHAR(1024) NULL COMMENT 'Error message if failed',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id),
    KEY idx_appsflyer_callback_af_id (appsflyer_id, created_at),
    KEY idx_appsflyer_callback_event (appsflyer_id, event_name, created_at),
    KEY idx_appsflyer_callback_customer (customer_user_id, created_at),
    KEY idx_appsflyer_callback_user (user_id, created_at),
    KEY idx_appsflyer_callback_device (device_no, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='AppsFlyer Push API callback storage';

-- Existing envs that already created the table without user_id / device_no.
SET @schema_name = DATABASE();
SET @has_user = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'appsflyer_callback' AND COLUMN_NAME = 'user_id'
);
SET @sql = IF(
    @has_user = 0,
    'ALTER TABLE appsflyer_callback ADD COLUMN user_id BIGINT UNSIGNED NULL COMMENT ''user_profile.id (resolved from local AF install)'' AFTER id',
    'SELECT ''skip: user_id'' AS migration_info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_device = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'appsflyer_callback' AND COLUMN_NAME = 'device_no'
);
SET @sql = IF(
    @has_device = 0,
    'ALTER TABLE appsflyer_callback ADD COLUMN device_no VARCHAR(64) NULL COMMENT ''Client device number (resolved from local AF install)'' AFTER user_id',
    'SELECT ''skip: device_no'' AS migration_info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
