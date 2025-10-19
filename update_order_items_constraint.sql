-- Update the order_items table to allow NULL for product_id (music reference)
-- This allows us to preserve order history even when music records are deleted

-- First, drop the existing NOT NULL constraint
ALTER TABLE order_items ALTER COLUMN product_id DROP NOT NULL;

-- Drop the existing foreign key constraint
ALTER TABLE order_items DROP CONSTRAINT IF EXISTS fkn5ghanj7yxegsb9cea7io2oqf;

-- Recreate the foreign key constraint with ON DELETE SET NULL
ALTER TABLE order_items
ADD CONSTRAINT fkn5ghanj7yxegsb9cea7io2oqf
FOREIGN KEY (product_id)
REFERENCES music(id)
ON DELETE SET NULL
ON UPDATE CASCADE;

-- Verify the changes
SELECT
    conname AS constraint_name,
    CASE confdeltype
        WHEN 'a' THEN 'NO ACTION'
        WHEN 'r' THEN 'RESTRICT'
        WHEN 'c' THEN 'CASCADE'
        WHEN 'n' THEN 'SET NULL'
        WHEN 'd' THEN 'SET DEFAULT'
        ELSE 'UNKNOWN'
    END AS on_delete_action
FROM pg_constraint
WHERE conname = 'fkn5ghanj7yxegsb9cea7io2oqf';
