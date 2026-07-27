-- Expand loan_lender_bill to store all /api/open/v1/loan/bill/list data[] fields (doc §21).
-- Drop derived next_due_amount. Keep user_id + external_interaction_id.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run (gated by contr_no existence).

SET @schema_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_lender_bill'
      AND COLUMN_NAME = 'contr_no'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE loan_lender_bill
        ADD COLUMN contr_no VARCHAR(64) NULL COMMENT ''Contract number'' AFTER bill_no,
        ADD COLUMN terms INT NULL COMMENT ''Total terms'' AFTER contr_no,
        ADD COLUMN currency VARCHAR(16) NULL COMMENT ''Currency'' AFTER apply_amt,
        ADD COLUMN user_name VARCHAR(128) NULL COMMENT ''User name'' AFTER currency,
        ADD COLUMN status_date BIGINT NULL COMMENT ''Bill status update time (epoch millis)'' AFTER user_name,
        ADD COLUMN fee_prepay_amt DECIMAL(18,2) NULL COMMENT ''Fee prepay / cut-head amount'' AFTER bill_status,
        ADD COLUMN lend_amt DECIMAL(18,2) NULL COMMENT ''Disbursement amount'' AFTER fee_prepay_amt,
        ADD COLUMN lend_time BIGINT NULL COMMENT ''Disbursement success time (epoch millis)'' AFTER lend_amt,
        ADD COLUMN pay_serial_no VARCHAR(64) NULL COMMENT ''Payment serial number'' AFTER lend_time,
        ADD COLUMN due_date BIGINT NULL COMMENT ''Due date (epoch millis)'' AFTER pay_serial_no,
        ADD COLUMN stamp_duty DECIMAL(18,2) NULL COMMENT ''Stamp duty'' AFTER due_date,
        ADD COLUMN principal DECIMAL(18,2) NULL COMMENT ''Principal'' AFTER stamp_duty,
        ADD COLUMN interest DECIMAL(18,2) NULL COMMENT ''Interest'' AFTER principal,
        ADD COLUMN fee1 DECIMAL(18,2) NULL COMMENT ''Service fee 1'' AFTER interest,
        ADD COLUMN fee2 DECIMAL(18,2) NULL COMMENT ''Service fee 2'' AFTER fee1,
        ADD COLUMN fee3 DECIMAL(18,2) NULL COMMENT ''Service fee 3'' AFTER fee2,
        ADD COLUMN fee1_tax DECIMAL(18,2) NULL COMMENT ''Service fee 1 tax'' AFTER fee3,
        ADD COLUMN fee2_tax DECIMAL(18,2) NULL COMMENT ''Service fee 2 tax'' AFTER fee1_tax,
        ADD COLUMN fee3_tax DECIMAL(18,2) NULL COMMENT ''Service fee 3 tax'' AFTER fee2_tax,
        ADD COLUMN pre_pen_interest DECIMAL(18,2) NULL COMMENT ''Pre-calculated penalty interest'' AFTER fee3_tax,
        ADD COLUMN pen_interest DECIMAL(18,2) NULL COMMENT ''Accumulated penalty interest'' AFTER pre_pen_interest,
        ADD COLUMN init_late_fee DECIMAL(18,2) NULL COMMENT ''Accumulated initial late fee'' AFTER pen_interest,
        ADD COLUMN term_no INT NULL COMMENT ''Current term number'' AFTER init_late_fee,
        ADD COLUMN should_stamp_duty DECIMAL(18,2) NULL COMMENT ''Stamp duty due'' AFTER term_due_date,
        ADD COLUMN should_principal DECIMAL(18,2) NULL COMMENT ''Principal due'' AFTER should_stamp_duty,
        ADD COLUMN should_interest DECIMAL(18,2) NULL COMMENT ''Interest due'' AFTER should_principal,
        ADD COLUMN should_fee1 DECIMAL(18,2) NULL COMMENT ''Fee1 due'' AFTER should_interest,
        ADD COLUMN should_fee2 DECIMAL(18,2) NULL COMMENT ''Fee2 due'' AFTER should_fee1,
        ADD COLUMN should_fee3 DECIMAL(18,2) NULL COMMENT ''Fee3 due'' AFTER should_fee2,
        ADD COLUMN should_fee1_tax DECIMAL(18,2) NULL COMMENT ''Fee1 tax due'' AFTER should_fee3,
        ADD COLUMN should_fee2_tax DECIMAL(18,2) NULL COMMENT ''Fee2 tax due'' AFTER should_fee1_tax,
        ADD COLUMN should_fee3_tax DECIMAL(18,2) NULL COMMENT ''Fee3 tax due'' AFTER should_fee2_tax,
        ADD COLUMN should_pen_interest DECIMAL(18,2) NULL COMMENT ''Penalty interest due'' AFTER should_fee3_tax,
        ADD COLUMN should_init_late_fee DECIMAL(18,2) NULL COMMENT ''Initial late fee due'' AFTER should_pen_interest,
        ADD COLUMN paid_stamp_duty DECIMAL(18,2) NULL COMMENT ''Paid stamp duty'' AFTER should_init_late_fee,
        ADD COLUMN paid_principal DECIMAL(18,2) NULL COMMENT ''Paid principal'' AFTER paid_stamp_duty,
        ADD COLUMN paid_interest DECIMAL(18,2) NULL COMMENT ''Paid interest'' AFTER paid_principal,
        ADD COLUMN paid_fee1 DECIMAL(18,2) NULL COMMENT ''Paid fee1'' AFTER paid_interest,
        ADD COLUMN paid_fee2 DECIMAL(18,2) NULL COMMENT ''Paid fee2'' AFTER paid_fee1,
        ADD COLUMN paid_fee3 DECIMAL(18,2) NULL COMMENT ''Paid fee3'' AFTER paid_fee2,
        ADD COLUMN paid_fee1_tax DECIMAL(18,2) NULL COMMENT ''Paid fee1 tax'' AFTER paid_fee3,
        ADD COLUMN paid_fee2_tax DECIMAL(18,2) NULL COMMENT ''Paid fee2 tax'' AFTER paid_fee1_tax,
        ADD COLUMN paid_fee3_tax DECIMAL(18,2) NULL COMMENT ''Paid fee3 tax'' AFTER paid_fee2_tax,
        ADD COLUMN paid_pen_interest DECIMAL(18,2) NULL COMMENT ''Paid penalty interest'' AFTER paid_fee3_tax,
        ADD COLUMN paid_init_late_fee DECIMAL(18,2) NULL COMMENT ''Paid initial late fee'' AFTER paid_pen_interest,
        ADD COLUMN paid_adv_settle_fee DECIMAL(18,2) NULL COMMENT ''Paid early settle fee'' AFTER paid_init_late_fee,
        ADD COLUMN reduction_stamp_duty DECIMAL(18,2) NULL COMMENT ''Reduced stamp duty'' AFTER paid_adv_settle_fee,
        ADD COLUMN reduction_principal DECIMAL(18,2) NULL COMMENT ''Reduced principal'' AFTER reduction_stamp_duty,
        ADD COLUMN reduction_interest DECIMAL(18,2) NULL COMMENT ''Reduced interest'' AFTER reduction_principal,
        ADD COLUMN reduction_fee1 DECIMAL(18,2) NULL COMMENT ''Reduced fee1'' AFTER reduction_interest,
        ADD COLUMN reduction_fee2 DECIMAL(18,2) NULL COMMENT ''Reduced fee2'' AFTER reduction_fee1,
        ADD COLUMN reduction_fee3 DECIMAL(18,2) NULL COMMENT ''Reduced fee3'' AFTER reduction_fee2,
        ADD COLUMN reduction_fee1_tax DECIMAL(18,2) NULL COMMENT ''Reduced fee1 tax'' AFTER reduction_fee3,
        ADD COLUMN reduction_fee2_tax DECIMAL(18,2) NULL COMMENT ''Reduced fee2 tax'' AFTER reduction_fee1_tax,
        ADD COLUMN reduction_fee3_tax DECIMAL(18,2) NULL COMMENT ''Reduced fee3 tax'' AFTER reduction_fee2_tax,
        ADD COLUMN reduction_pen_interest DECIMAL(18,2) NULL COMMENT ''Reduced penalty interest'' AFTER reduction_fee3_tax,
        ADD COLUMN reduction_init_late_fee DECIMAL(18,2) NULL COMMENT ''Reduced initial late fee'' AFTER reduction_pen_interest,
        ADD COLUMN reduction_adv_settle_fee DECIMAL(18,2) NULL COMMENT ''Reduced early settle fee'' AFTER reduction_init_late_fee,
        ADD COLUMN overdue_days INT NULL COMMENT ''Overdue days'' AFTER reduction_adv_settle_fee,
        ADD COLUMN first_overdue_day BIGINT NULL COMMENT ''First overdue date (epoch millis)'' AFTER overdue_days,
        ADD COLUMN last_repay_time BIGINT NULL COMMENT ''Last repay time (epoch millis)'' AFTER first_overdue_day,
        ADD COLUMN max_overdue_days INT NULL COMMENT ''Historical max overdue days'' AFTER last_repay_time,
        ADD COLUMN paid_out_date BIGINT NULL COMMENT ''Settle date (epoch millis)'' AFTER max_overdue_days,
        ADD COLUMN adv_sette_flag VARCHAR(16) NULL COMMENT ''Early settle flag Y/N'' AFTER paid_out_date',
    'SELECT ''skip: loan_lender_bill full bill fields already added'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'loan_lender_bill'
      AND COLUMN_NAME = 'next_due_amount'
);
SET @ddl = IF(
    @col_exists > 0,
    'ALTER TABLE loan_lender_bill DROP COLUMN next_due_amount',
    'SELECT ''skip: loan_lender_bill.next_due_amount drop'' AS migration_info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
