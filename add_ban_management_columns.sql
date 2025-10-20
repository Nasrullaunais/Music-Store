-- Add ban management columns to customers table
ALTER TABLE customers
ADD COLUMN is_banned BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN ban_reason VARCHAR(500),
ADD COLUMN banned_until TIMESTAMP,
ADD COLUMN banned_by VARCHAR(100),
ADD COLUMN banned_at TIMESTAMP;

-- Add ban management columns to artists table
ALTER TABLE artists
ADD COLUMN is_banned BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN ban_reason VARCHAR(500),
ADD COLUMN banned_until TIMESTAMP,
ADD COLUMN banned_by VARCHAR(100),
ADD COLUMN banned_at TIMESTAMP;

-- Create indexes for better query performance
CREATE INDEX idx_customers_is_banned ON customers(is_banned);
CREATE INDEX idx_customers_banned_until ON customers(banned_until);
CREATE INDEX idx_artists_is_banned ON artists(is_banned);
CREATE INDEX idx_artists_banned_until ON artists(banned_until);

-- Add comments for documentation
COMMENT ON COLUMN customers.is_banned IS 'Indicates if the customer account is currently banned';
COMMENT ON COLUMN customers.ban_reason IS 'Reason provided by admin for the ban';
COMMENT ON COLUMN customers.banned_until IS 'Timestamp when the ban will expire';
COMMENT ON COLUMN customers.banned_by IS 'Username of the admin who banned the user';
COMMENT ON COLUMN customers.banned_at IS 'Timestamp when the ban was applied';

COMMENT ON COLUMN artists.is_banned IS 'Indicates if the artist account is currently banned';
COMMENT ON COLUMN artists.ban_reason IS 'Reason provided by admin for the ban';
COMMENT ON COLUMN artists.banned_until IS 'Timestamp when the ban will expire';
COMMENT ON COLUMN artists.banned_by IS 'Username of the admin who banned the user';
COMMENT ON COLUMN artists.banned_at IS 'Timestamp when the ban was applied';

