-- Idempotency key for POST /credit/apply; apply_id is server-generated.
ALTER TABLE credit_application
    ADD COLUMN request_id VARCHAR(64) NOT NULL COMMENT 'Idempotency request identifier' AFTER apply_id,
    ADD UNIQUE KEY uk_credit_application_request_id (request_id);
