-- Simple SQL to add refresh token columns
-- Run each statement separately and ignore errors if columns already exist

-- Add refresh_token column
ALTER TABLE account ADD COLUMN refresh_token VARCHAR(255) NULL;

-- Add refresh_token_expiry_date column  
ALTER TABLE account ADD COLUMN refresh_token_expiry_date TIMESTAMP NULL;

-- Create index for faster lookup
CREATE INDEX idx_account_refresh_token ON account(refresh_token);

-- Drop the old refresh_token table if it exists
DROP TABLE IF EXISTS refresh_token;

-- Note: If you get "Duplicate column name" error, 
-- it means the columns already exist and you can ignore the error.