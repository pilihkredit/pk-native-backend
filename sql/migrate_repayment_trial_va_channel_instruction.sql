-- Fix: VA channel instruction can exceed VARCHAR(1024) from lender payload.
-- Run once per environment that already created repayment_trial_va_channel.

ALTER TABLE repayment_trial_va_channel
    MODIFY COLUMN instruction TEXT NULL COMMENT 'Channel instruction';
