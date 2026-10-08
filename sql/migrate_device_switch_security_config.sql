-- Device-switch security knobs moved from application.yml to app_config (values apply
-- immediately, no restart). Run against the PK business DB.

INSERT INTO app_config (`key`, `value`)
VALUES (
    'device_switch_security',
    CAST(
        '{
            "livenessLicenseMaxPerIp": 100,
            "livenessLicenseMaxPerDevice": 5,
            "livenessLicenseWindowMinutes": 10,
            "livenessLicenseBlockMinutes": 15,
            "faceVerifyMaxFailures": 5,
            "faceVerifyFailWindowMinutes": 60,
            "livenessSessionDurationSeconds": 600,
            "deviceNoMaxLength": 128
        }' AS JSON
    )
)
ON DUPLICATE KEY UPDATE
    `value` = VALUES(`value`);
