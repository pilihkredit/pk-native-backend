-- AppsFlyer S2S config aligned with Flutter AppsFlyerSdk.init options.
-- Run after migrate_appsflyer_s2s_tables.sql
-- Event types are no longer gated by adjust_event_config; all lender eventTypes are reported.

-- adjust_config: one row per platform (app_token + dev key match client SDK)
INSERT INTO adjust_config (app_token, api_token, base_url, timeout, is_active, os_name)
VALUES
    (
        'com.pilihid.kreditid.pilihkredit',
        'HeifSG89N8czCxtsNEgpqJ',
        'https://api2.appsflyer.com/inappevent/com.pilihid.kreditid.pilihkredit',
        30000,
        1,
        'Android'
    ),
    (
        '6749564952',
        'ErDDv7KwyF6kjTMMNqQKJJ',
        'https://api2.appsflyer.com/inappevent/id6749564952',
        30000,
        1,
        'iOS'
    );
