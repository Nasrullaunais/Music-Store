-- Add created_at column to all user tables
-- Migration script to add created_at timestamps

-- Add created_at column to customers table
ALTER TABLE customers ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add created_at column to artists table
ALTER TABLE artists ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add created_at column to admins table
ALTER TABLE admins ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add created_at column to staff table
ALTER TABLE staff ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Update existing records to have a default created_at value (current timestamp)
UPDATE customers SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE artists SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE admins SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE staff SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;

-- Make the columns NOT NULL after setting default values
ALTER TABLE customers ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE artists ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE admins ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE staff ALTER COLUMN created_at SET NOT NULL;
