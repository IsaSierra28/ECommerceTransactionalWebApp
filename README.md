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

## Quiz 2 - REST API (backend)

The folder `backend/` contains the REST API made with Spring Boot.

Technologies: Java 25 (Java 21 or above), Spring Boot 4.1, Spring MVC (REST), Spring Data JPA (Hibernate),
Spring Security, Jakarta Bean Validation, Swagger (springdoc-openapi) and Maven.

### Backend Setup

1. In SQL Server, enable **SQL Server and Windows Authentication mode** and **TCP/IP on port 1433**
   (SQL Server Configuration Manager), then restart SQL Server.
2. Create a login for the application (choose your own password, do not save it in any file):

   ```sql
   CREATE LOGIN ecommerce_app WITH PASSWORD = '<your password>';
   USE ECommerceDB;
   CREATE USER ecommerce_app FOR LOGIN ecommerce_app;
   ALTER ROLE db_datareader ADD MEMBER ecommerce_app;
   ALTER ROLE db_datawriter ADD MEMBER ecommerce_app;
   ```

3. Create the Windows environment variable `DB_PASSWORD` with that password.
   `application.properties` reads it with `${DB_PASSWORD}`, so the password is never in the code or on GitHub.
   (Optional: `DB_USERNAME`, default `ecommerce_app`.)
4. Run the application from IntelliJ (`EcommerceApplication`) or with:

   ```bash
   cd backend
   mvnw spring-boot:run
   ```

5. Build and run the tests with `mvnw clean package`.

The API runs on `http://localhost:8080`.

### Swagger

- Swagger UI: **http://localhost:8080/swagger-ui.html**
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Click **Authorize** and log in with a test account (for example `maria` / `Password123!`).

### Main Endpoints

| Method | Endpoint | Who |
|---|---|---|
| GET | /api/products, /api/products/{id}, /api/categories | Everybody |
| POST, PUT, DELETE | /api/products, /api/products/{id} | ADMIN, VENDOR (own products) |
| GET, POST, PUT, DELETE | /api/vendors, /api/vendors/{id} | ADMIN |
| PATCH | /api/vendors/{id}/activate, /api/vendors/{id}/deactivate | ADMIN |
| GET, POST, PUT, DELETE | /api/customers, /api/customers/{id} | ADMIN |
| GET | /api/customers/me, /api/customers/me/addresses | CUSTOMER |
| POST | /api/orders (purchase transaction) | CUSTOMER |
| GET | /api/orders, /api/orders/{id} | ADMIN (all), VENDOR (their products), CUSTOMER (own) |
| GET | /api/invoices, /api/invoices/{id} | ADMIN (all), CUSTOMER (own) |
| GET | /api/auth/me | Any logged user |

Test payment data: credit card `4111 1111 1111 1111`, expiry `12/30`, CVV `123`.
Bank transfer: institution `001`, transit `12345`, account `1234567`.

### Architecture

`Controller -> Service -> Repository -> JPA/Hibernate -> SQL Server`

- `controller/` only handles HTTP (requests, DTOs, status codes).
- `service/` has all the business rules and the transactions.
- `repository/` uses Spring Data JPA.
- `model/` has the JPA entities, `dto/` the request/response objects with validation,
  `exception/` the centralized error handling, `security/` and `config/` the configuration.

**Case 1 - Service only:** `ProductService` is a concrete `@Service` class. There is only one way to manage
products, so an interface would not add anything.

**Case 2 - Interface + two implementations:** `PaymentService` has two implementations with different rules:
`CreditCardPaymentServiceImpl` (card number with Luhn check, expiry date, CVV, maximum 5,000) and
`BankTransferPaymentServiceImpl` (institution, transit and account number, no maximum).
`OrderService` receives both with `@Qualifier("creditCardPaymentService")` and
`@Qualifier("bankTransferPaymentService")` and chooses one with the payment method of the order.

### Transaction and Rollback

`OrderService.placeOrder()` uses `@Transactional`. In one transaction it: verifies the customer and the address,
verifies the products, calculates the total, creates the order, checks the inventory, creates the order items,
updates the inventory, creates the invoice, processes the payment and records it.
If any step fails (insufficient inventory, insufficient payment, invalid card...), an exception is thrown and
**everything is rolled back**: no order, no order items, no inventory change, no invoice and no payment.

### Concurrency

`Product` has a `@Version` column (**optimistic locking**). When two customers buy the last unit at the same time,
only the first update is accepted. The second transaction fails (Hibernate detects the changed version, or SQL Server
detects the conflict with its locks) and is rolled back with HTTP 409, so the inventory is never oversold.

### Error Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) returns the same JSON for every error:
400 (invalid input), 401 (not logged in), 403 (role not allowed), 404 (not found),
409 (insufficient inventory or payment, duplicated data, concurrent purchase), 500 (unexpected error).

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
