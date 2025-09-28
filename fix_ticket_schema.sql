-- SQL commands to fix the ticket system database schema
-- Run these in your PostgreSQL database

-- First, remove the NOT NULL constraint and drop the old columns
ALTER TABLE tickets DROP COLUMN IF EXISTS message;
ALTER TABLE tickets DROP COLUMN IF EXISTS reply;

-- Verify the changes
\d tickets;

-- Optional: If you want to see the current structure
SELECT column_name, is_nullable, data_type
FROM information_schema.columns
WHERE table_name = 'tickets';
