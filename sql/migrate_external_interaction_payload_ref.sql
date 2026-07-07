-- Expand external_interaction payload storage so large lender responses (e.g. loan/trial) are not truncated.
ALTER TABLE external_interaction
    MODIFY COLUMN request_ref MEDIUMTEXT NULL COMMENT 'Request payload storage reference',
    MODIFY COLUMN response_ref MEDIUMTEXT NULL COMMENT 'Response payload storage reference';
