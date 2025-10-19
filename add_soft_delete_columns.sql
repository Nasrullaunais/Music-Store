-- Add soft delete columns to the music table
-- This allows marking music as deleted without removing it from the database
-- Preserves all foreign key relationships and order history

-- Add is_deleted column (default false for existing records)
ALTER TABLE music ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT false;

-- Add deleted_at column (nullable, only set when deleted)
ALTER TABLE music ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Add deleted_by column (who deleted it - admin username)
ALTER TABLE music ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255);

-- Update existing records to ensure is_deleted is false
UPDATE music SET is_deleted = false WHERE is_deleted IS NULL;

-- Create index on is_deleted for better query performance
CREATE INDEX IF NOT EXISTS idx_music_is_deleted ON music(is_deleted);

-- Verify the changes
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'music'
AND column_name IN ('is_deleted', 'deleted_at', 'deleted_by')
ORDER BY column_name;

