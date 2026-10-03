-- Step 5: Test the trigger

USE ECommerceDB;
GO

-- 1. See the products BEFORE the change
SELECT product_id, name, quantity, status FROM product;

-- 2. Change many products at the same time (inside a transaction)
BEGIN TRANSACTION;

UPDATE product SET quantity = 0 WHERE sku IN ('TS-KEY-001', 'BW-JAV-001', 'BW-SQL-002');
UPDATE product SET quantity = 5 WHERE sku = 'TS-LAM-005';

-- 3. See the products AFTER the change
SELECT product_id, name, quantity, status FROM product;

-- 4. Undo the changes, so the sample data stays the same
ROLLBACK TRANSACTION;

-- 5. Check that everything is back to normal
SELECT product_id, name, quantity, status FROM product;
GO