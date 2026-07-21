-- user_profile_af / user_profile_tongdun: append history + half-year RANGE COLUMNS partitions (~5 years).
-- Naming: pYYYYh1 = Jan–Jun (LESS THAN Jul 1), pYYYYh2 = Jul–Dec (LESS THAN next Jan 1).
-- Prebuilt: 2026H2 .. 2031H1, then pmax. Before data piles into pmax, extend e.g.:
--   ALTER TABLE user_profile_af REORGANIZE PARTITION pmax INTO (
--     PARTITION p2031h2 VALUES LESS THAN ('2032-01-01'),
--     PARTITION p2032h1 VALUES LESS THAN ('2032-07-01'),
--     PARTITION pmax VALUES LESS THAN (MAXVALUE)
--   );
-- Archive: DROP/EXCHANGE old half-year partitions.
-- Run on MySQL 8+ with utf8mb4.

CREATE TABLE IF NOT EXISTS user_profile_af (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number',
    appsflyer_id VARCHAR(64) NOT NULL COMMENT 'AppsFlyer ID',
    advertising_id VARCHAR(128) NULL COMMENT 'Advertising ID',
    android_id VARCHAR(128) NULL COMMENT 'Android ID',
    attributed_touch_time VARCHAR(32) NULL COMMENT 'Attributed touch time yyyy-MM-dd HH:mm:ss[.SSS]',
    gp_click_time VARCHAR(32) NULL COMMENT 'Google Play click time',
    install_time VARCHAR(32) NULL COMMENT 'Install time',
    media_source VARCHAR(128) NULL COMMENT 'Media source',
    af_prt VARCHAR(128) NULL COMMENT 'Partner identifier',
    af_adset_id VARCHAR(128) NULL COMMENT 'Adset ID',
    af_adset VARCHAR(128) NULL COMMENT 'Adset name',
    af_siteid VARCHAR(128) NULL COMMENT 'Site ID',
    af_c_id VARCHAR(128) NULL COMMENT 'Campaign ID',
    campaign VARCHAR(128) NULL COMMENT 'Campaign',
    app_version VARCHAR(64) NULL COMMENT 'App version',
    app_id VARCHAR(128) NULL COMMENT 'App ID / package',
    device_type VARCHAR(128) NULL COMMENT 'Device type',
    os_version VARCHAR(64) NULL COMMENT 'OS version',
    country_code VARCHAR(16) NULL COMMENT 'Country code',
    city VARCHAR(128) NULL COMMENT 'City',
    postal_code VARCHAR(32) NULL COMMENT 'Postal code',
    ip VARCHAR(64) NULL COMMENT 'IP',
    operator VARCHAR(128) NULL COMMENT 'Carrier',
    device_category VARCHAR(64) NULL COMMENT 'Device category',
    platform VARCHAR(32) NULL COMMENT 'Platform',
    device_model VARCHAR(128) NULL COMMENT 'Device model',
    idfv VARCHAR(128) NULL COMMENT 'iOS IDFV',
    idfa VARCHAR(128) NULL COMMENT 'iOS IDFA',
    af_ad VARCHAR(128) NULL COMMENT 'Ad name',
    af_channel VARCHAR(128) NULL COMMENT 'AF channel',
    attributed_touch_type VARCHAR(64) NULL COMMENT 'Attributed touch type',
    af_ad_id VARCHAR(128) NULL COMMENT 'AF ad ID',
    af_ad_type VARCHAR(64) NULL COMMENT 'AF ad type',
    contributor1_touch_type VARCHAR(64) NULL COMMENT 'Contributor1 touch type',
    contributor1_touch_time VARCHAR(32) NULL COMMENT 'Contributor1 touch time',
    contributor1_af_prt VARCHAR(128) NULL COMMENT 'Contributor1 partner',
    contributor1_match_type VARCHAR(64) NULL COMMENT 'Contributor1 match type',
    contributor1_engagement_type VARCHAR(64) NULL COMMENT 'Contributor1 engagement type',
    bundle_id VARCHAR(128) NULL COMMENT 'Bundle ID',
    match_type VARCHAR(64) NULL COMMENT 'Match type',
    gp_install_begin VARCHAR(32) NULL COMMENT 'GP install begin time',
    module_status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED' COMMENT 'Module status',
    request_id VARCHAR(64) NOT NULL COMMENT 'Client request id for idempotency',
    last_lender_request_json JSON NULL COMMENT 'Last lender user/info/upsert request audit JSON',
    last_lender_response_json JSON NULL COMMENT 'Last lender user/info/upsert response data JSON',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time (partition key)',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id, created_at),
    KEY idx_user_profile_af_request (request_id, created_at),
    KEY idx_user_profile_af_profile (profile_id, created_at),
    KEY idx_user_profile_af_appsflyer (appsflyer_id, created_at),
    KEY idx_user_profile_af_advertising (advertising_id, created_at),
    KEY idx_user_profile_af_ad_id (af_ad_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AppsFlyer install history synced to lender'
PARTITION BY RANGE COLUMNS (created_at) (
    PARTITION p2026h2 VALUES LESS THAN ('2027-01-01'),
    PARTITION p2027h1 VALUES LESS THAN ('2027-07-01'),
    PARTITION p2027h2 VALUES LESS THAN ('2028-01-01'),
    PARTITION p2028h1 VALUES LESS THAN ('2028-07-01'),
    PARTITION p2028h2 VALUES LESS THAN ('2029-01-01'),
    PARTITION p2029h1 VALUES LESS THAN ('2029-07-01'),
    PARTITION p2029h2 VALUES LESS THAN ('2030-01-01'),
    PARTITION p2030h1 VALUES LESS THAN ('2030-07-01'),
    PARTITION p2030h2 VALUES LESS THAN ('2031-01-01'),
    PARTITION p2031h1 VALUES LESS THAN ('2031-07-01'),
    PARTITION pmax VALUES LESS THAN (MAXVALUE)
);

CREATE TABLE IF NOT EXISTS user_profile_tongdun (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    mobile_no VARCHAR(32) NOT NULL COMMENT 'Account owner mobile number',
    scene_type VARCHAR(16) NOT NULL COMMENT 'LOGIN|SIGNUP|IDENTITY|LOAN|CREDIT',
    tongdun_key VARCHAR(256) NOT NULL COMMENT 'Tongdun device fingerprint key',
    module_status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED' COMMENT 'Module status',
    request_id VARCHAR(64) NOT NULL COMMENT 'Client request id for idempotency',
    last_lender_request_json JSON NULL COMMENT 'Last lender user/info/upsert request audit JSON',
    last_lender_response_json JSON NULL COMMENT 'Last lender user/info/upsert response data JSON',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time (partition key)',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (id, created_at),
    KEY idx_user_profile_tongdun_request (request_id, created_at),
    KEY idx_user_profile_tongdun_profile (profile_id, created_at),
    KEY idx_user_profile_tongdun_profile_scene (profile_id, scene_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tongdun device fingerprint history synced to lender'
PARTITION BY RANGE COLUMNS (created_at) (
    PARTITION p2026h2 VALUES LESS THAN ('2027-01-01'),
    PARTITION p2027h1 VALUES LESS THAN ('2027-07-01'),
    PARTITION p2027h2 VALUES LESS THAN ('2028-01-01'),
    PARTITION p2028h1 VALUES LESS THAN ('2028-07-01'),
    PARTITION p2028h2 VALUES LESS THAN ('2029-01-01'),
    PARTITION p2029h1 VALUES LESS THAN ('2029-07-01'),
    PARTITION p2029h2 VALUES LESS THAN ('2030-01-01'),
    PARTITION p2030h1 VALUES LESS THAN ('2030-07-01'),
    PARTITION p2030h2 VALUES LESS THAN ('2031-01-01'),
    PARTITION p2031h1 VALUES LESS THAN ('2031-07-01'),
    PARTITION pmax VALUES LESS THAN (MAXVALUE)
);
