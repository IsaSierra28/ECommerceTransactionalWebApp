-- Step 4: Insert sample data

USE ECommerceDB;
GO

-- Roles
INSERT INTO role (name) VALUES
('ADMIN'),
('VENDOR'),
('CUSTOMER');
GO

-- Users
-- All the test users have the password: Password123!
-- The password is saved encrypted with BCrypt (the same method Spring Security uses).
INSERT INTO app_user (role_id, username, email, password_hash) VALUES
((SELECT role_id FROM role WHERE name = 'ADMIN'),    'admin',     'admin@ecommerce.com',     '$2a$10$KDVBf60cglyvuy171RuCwu.WvsMLeL7tsLAtT4q4iPEAazheSDk5G'),
((SELECT role_id FROM role WHERE name = 'VENDOR'),   'techstore', 'contact@techstore.com',   '$2a$10$KDVBf60cglyvuy171RuCwu.WvsMLeL7tsLAtT4q4iPEAazheSDk5G'),
((SELECT role_id FROM role WHERE name = 'VENDOR'),   'bookworld', 'contact@bookworld.com',   '$2a$10$KDVBf60cglyvuy171RuCwu.WvsMLeL7tsLAtT4q4iPEAazheSDk5G'),
((SELECT role_id FROM role WHERE name = 'CUSTOMER'), 'maria',     'maria.lopez@email.com',   '$2a$10$KDVBf60cglyvuy171RuCwu.WvsMLeL7tsLAtT4q4iPEAazheSDk5G'),
((SELECT role_id FROM role WHERE name = 'CUSTOMER'), 'john',      'john.smith@email.com',    '$2a$10$KDVBf60cglyvuy171RuCwu.WvsMLeL7tsLAtT4q4iPEAazheSDk5G');
GO

-- Vendors
INSERT INTO vendor (user_id, company_name, phone) VALUES
((SELECT user_id FROM app_user WHERE username = 'techstore'), 'TechStore Inc.', '514-555-0101'),
((SELECT user_id FROM app_user WHERE username = 'bookworld'), 'BookWorld Ltd.', '514-555-0202');
GO

-- Customers
INSERT INTO customer (user_id, first_name, last_name, phone) VALUES
((SELECT user_id FROM app_user WHERE username = 'maria'), 'Maria', 'Lopez', '438-555-0303'),
((SELECT user_id FROM app_user WHERE username = 'john'),  'John',  'Smith', NULL);
GO

-- Addresses
INSERT INTO address (customer_id, street, city, province, postal_code, country) VALUES
((SELECT c.customer_id FROM customer c JOIN app_user u ON c.user_id = u.user_id WHERE u.username = 'maria'), '123 Saint-Denis Street', 'Montreal', 'Quebec',  'H2X 1K4', 'Canada'),
((SELECT c.customer_id FROM customer c JOIN app_user u ON c.user_id = u.user_id WHERE u.username = 'maria'), '45 University Avenue',   'Montreal', 'Quebec',  'H3A 0G4', 'Canada'),
((SELECT c.customer_id FROM customer c JOIN app_user u ON c.user_id = u.user_id WHERE u.username = 'john'),  '890 King Street West',   'Toronto',  'Ontario', 'M5V 1J2', 'Canada');
GO

-- Categories
INSERT INTO category (name, description) VALUES
('Electronics', 'Computers, accessories and gadgets'),
('Books',       'Printed books of all kinds'),
('Home',        'Products for the house');
GO


-- Products
-- Some products have a special quantity to test the application later:
--   quantity 0 -> the trigger sets OUT_OF_STOCK
--   quantity 1 -> to test that two customers cannot buy the last unit
--   quantity 3 -> to test a purchase of 5 units that must be rejected (rollback)
INSERT INTO product (vendor_id, category_id, sku, name, description, price, quantity) VALUES
((SELECT vendor_id FROM vendor WHERE company_name = 'TechStore Inc.'), (SELECT category_id FROM category WHERE name = 'Electronics'), 'TS-KEY-001', 'Wireless Keyboard',   'Bluetooth keyboard with long battery life',  49.99, 18),
((SELECT vendor_id FROM vendor WHERE company_name = 'TechStore Inc.'), (SELECT category_id FROM category WHERE name = 'Electronics'), 'TS-MOU-002', 'Wireless Mouse',      'Ergonomic mouse with USB receiver',          19.99, 24),
((SELECT vendor_id FROM vendor WHERE company_name = 'TechStore Inc.'), (SELECT category_id FROM category WHERE name = 'Electronics'), 'TS-MON-003', '24 inch Monitor',     'Full HD monitor with HDMI',                 159.99,  3),
((SELECT vendor_id FROM vendor WHERE company_name = 'TechStore Inc.'), (SELECT category_id FROM category WHERE name = 'Electronics'), 'TS-HEA-004', 'Headphones',          'Noise cancelling headphones',                89.50,  1),
((SELECT vendor_id FROM vendor WHERE company_name = 'TechStore Inc.'), (SELECT category_id FROM category WHERE name = 'Home'),        'TS-LAM-005', 'LED Desk Lamp',       'Desk lamp with 3 light levels',              29.99,  0),
((SELECT vendor_id FROM vendor WHERE company_name = 'BookWorld Ltd.'), (SELECT category_id FROM category WHERE name = 'Books'),       'BW-JAV-001', 'Learning Java',       'Java programming for beginners',             45.00, 10),
((SELECT vendor_id FROM vendor WHERE company_name = 'BookWorld Ltd.'), (SELECT category_id FROM category WHERE name = 'Books'),       'BW-SQL-002', 'SQL Server Basics',   'Introduction to databases with SQL Server',  39.95,  7),
((SELECT vendor_id FROM vendor WHERE company_name = 'BookWorld Ltd.'), (SELECT category_id FROM category WHERE name = 'Books'),       'BW-REA-003', 'React for Beginners', 'Build web pages with React and TypeScript',  42.50,  0);
GO

-- One completed order of Maria: 2 Wireless Keyboards + 1 Wireless Mouse
-- Total = 2 x 49.99 + 1 x 19.99 = 119.97
INSERT INTO customer_order (customer_id, address_id, status, total) VALUES
((SELECT c.customer_id FROM customer c JOIN app_user u ON c.user_id = u.user_id WHERE u.username = 'maria'),
 (SELECT a.address_id FROM address a WHERE a.street = '123 Saint-Denis Street'),
 'DELIVERED', 119.97);
GO

INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES
((SELECT MAX(order_id) FROM customer_order), (SELECT product_id FROM product WHERE sku = 'TS-KEY-001'), 2, 49.99),
((SELECT MAX(order_id) FROM customer_order), (SELECT product_id FROM product WHERE sku = 'TS-MOU-002'), 1, 19.99);
GO

INSERT INTO invoice (order_id, invoice_number, total) VALUES
((SELECT MAX(order_id) FROM customer_order), 'INV-2026-00001', 119.97);
GO

INSERT INTO payment (order_id, payment_method, amount) VALUES
((SELECT MAX(order_id) FROM customer_order), 'CREDIT_CARD', 119.97);
GO