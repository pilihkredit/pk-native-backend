-- Pendanaan production provider configuration (idempotent).
-- Apply against the production database only.
-- Replace lender base_url / client credentials with production values from Pendanaan before running.

INSERT INTO pk_provider (
    provider_code,
    provider_name,
    status,
    base_url,
    callback_base_url,
    config_json
) VALUES (
    'pendanaan',
    'Pendanaan Production',
    'ACTIVE',
    'https://REPLACE_WITH_PROD_LENDER_BASE_URL',
    'https://api.pilihkredit.id/api/v1',
    JSON_OBJECT('mode', 'http', 'appName', 'KtaKilatPlus')
) ON DUPLICATE KEY UPDATE
    provider_name = VALUES(provider_name),
    status = VALUES(status),
    base_url = VALUES(base_url),
    callback_base_url = VALUES(callback_base_url),
    config_json = VALUES(config_json);

DELETE FROM pk_api_credential WHERE provider_code = 'pendanaan';

INSERT INTO pk_api_credential (
    provider_code,
    client_id,
    client_secret_ref,
    callback_client_id,
    callback_secret_ref,
    effective_at,
    status
) VALUES (
    'pendanaan',
    'REPLACE_WITH_PROD_CLIENT_ID',
    'REPLACE_WITH_PROD_CLIENT_SECRET',
    'REPLACE_WITH_PROD_CALLBACK_CLIENT_ID',
    'REPLACE_WITH_PROD_CALLBACK_CLIENT_SECRET',
    CURRENT_TIMESTAMP(3),
    'ACTIVE'
);
