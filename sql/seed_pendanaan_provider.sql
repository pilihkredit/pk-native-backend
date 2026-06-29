-- Pendanaan provider seed for local Docker and test RDS (idempotent).
-- Points at Pendanaan test gateway; callback_base_url is the deployed test API (not localhost).

INSERT INTO pk_provider (
    provider_code,
    provider_name,
    status,
    base_url,
    callback_base_url,
    config_json
) VALUES (
    'pendanaan',
    'Pendanaan Test',
    'ACTIVE',
    'http://gateway.test.ptnadmin.com/ktaid',
    'https://api-test.pilihkredit.id/api/v1',
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
    'oc_ybmk9xvr8hockw5j1zexx3q5',
    'vEgB3NcR5x9wx9ZBB4ufCcPbSKcF6i6P',
    NULL,
    NULL,
    CURRENT_TIMESTAMP(3),
    'ACTIVE'
);
