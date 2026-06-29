ALTER TABLE user_profile
    CHANGE COLUMN access_token_expires_in access_token_expires_at DATETIME(3) NULL COMMENT 'Access token expiry time';
