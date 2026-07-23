-- Max bank cards a user may bind (active / non-deleted).
-- Safe to re-run.

INSERT INTO app_config (`key`, `value`)
VALUES (
    'bank_card_max_count',
    CAST('5' AS JSON)
)
ON DUPLICATE KEY UPDATE
    `value` = VALUES(`value`);
