-- Face verify ticket TTL (minutes). Safe to re-run.
INSERT INTO app_config (`key`, `value`)
VALUES (
    'face_verify_ticket_ttl',
    CAST('{"ttlMinutes":5}' AS JSON)
)
ON DUPLICATE KEY UPDATE
    `value` = VALUES(`value`);
