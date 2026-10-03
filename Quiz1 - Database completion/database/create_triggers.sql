-- Step 3: Create the triggers

USE ECommerceDB;
GO

-- Trigger: when the quantity of a product is 0, the status changes to OUT_OF_STOCK.
-- When the quantity is more than 0, the status changes to IN_STOCK.
-- It works when one product or many products are inserted or updated at the same time.
CREATE TRIGGER trg_product_update_status
ON product
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE p
    SET p.status = CASE
                       WHEN p.quantity = 0 THEN 'OUT_OF_STOCK'
                       ELSE 'IN_STOCK'
                   END
    FROM product p
    INNER JOIN inserted i ON p.product_id = i.product_id;
END;
GO