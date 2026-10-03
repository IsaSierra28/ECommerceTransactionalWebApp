# E-Commerce Transactional Web Application

Final project: an online store where vendors sell products and customers buy them.
The application uses React + TypeScript (frontend), Java Spring Boot (backend) and
Microsoft SQL Server (database).

## Quiz 1 - Database

The folder `Quiz1 - Database completion` contains:

- `Conceptual-Schema.pdf` - entities, attributes and relationships (ER diagram)
- `Logical-Schema.pdf` - tables, primary keys, foreign keys and normalization
- `database/` - SQL Server scripts (physical implementation)

The source files of the documents are in the folder `docs/`.

## Database Setup

Requirements: Microsoft SQL Server and SQL Server Management Studio (SSMS).

Connect to `localhost` with Windows Authentication and run the scripts in this order:

1. `create_database.sql` - creates the database ECommerceDB
2. `create_tables.sql` - creates the 11 tables with their constraints
3. `create_triggers.sql` - creates the trigger that updates the product status
4. `insert_sample_data.sql` - inserts test data
5. `test_trigger.sql` - shows that the trigger works

No passwords are saved in the scripts.

## Test Accounts

All test users have the password `Password123!` (saved encrypted with BCrypt).

| Username | Role |
|---|---|
| admin | ADMIN |
| techstore | VENDOR |
| bookworld | VENDOR |
| maria | CUSTOMER |
| john | CUSTOMER |

## Author

Isabela Sierra
