-- Migration: Ensure employer_master has serial_no column
ALTER TABLE payment_flow.employer_master
    ADD COLUMN IF NOT EXISTS serial_no VARCHAR(64);

DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'employer_master_serial_no_key'
    ) THEN
        ALTER TABLE payment_flow.employer_master
            ADD CONSTRAINT employer_master_serial_no_key UNIQUE (serial_no);
    END IF;
END;
$$;
