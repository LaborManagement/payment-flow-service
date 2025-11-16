-- Migration: Add owner_name column to employer_master
ALTER TABLE payment_flow.employer_master
ADD COLUMN owner_name VARCHAR(120);
