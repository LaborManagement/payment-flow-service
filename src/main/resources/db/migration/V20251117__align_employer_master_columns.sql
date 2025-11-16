-- Migration: Align employer_master columns with standardized upload format

ALTER TABLE payment_flow.employer_master
    ADD COLUMN IF NOT EXISTS employer_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS email_id VARCHAR(150),
    ADD COLUMN IF NOT EXISTS aadhar_number VARCHAR(12),
    ADD COLUMN IF NOT EXISTS virtual_bank_account_number VARCHAR(64);

DO
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'payment_flow'
          AND table_name = 'employer_master'
          AND column_name = 'registration_no'
    )
       AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'payment_flow'
          AND table_name = 'employer_master'
          AND column_name = 'registration_number'
    ) THEN
        ALTER TABLE payment_flow.employer_master
            RENAME COLUMN registration_no TO registration_number;
    END IF;
END;
$$;
