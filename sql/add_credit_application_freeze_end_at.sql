-- User freeze end time from lender; populated when credit status=REJECTED (REFUSED).
ALTER TABLE credit_application
    ADD COLUMN freeze_end_at DATETIME(3) NULL COMMENT 'User freeze end time from lender' AFTER finalized_at;
