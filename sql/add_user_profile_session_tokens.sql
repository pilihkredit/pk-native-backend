ALTER TABLE user_profile
    ADD COLUMN access_token VARCHAR(2048) NULL COMMENT 'Latest issued JWT access token' AFTER last_synced_at,
    ADD COLUMN refresh_token VARCHAR(128) NULL COMMENT 'Latest issued refresh token' AFTER access_token,
    ADD COLUMN access_token_expires_at DATETIME(3) NULL COMMENT 'Access token expiry time' AFTER refresh_token;
