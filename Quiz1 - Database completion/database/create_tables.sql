-- Step 2: Create the tables

USE ECommerceDB;
GO

-- Table: role
CREATE TABLE role (
    role_id  INT IDENTITY(1,1) NOT NULL,
    name     NVARCHAR(20)      NOT NULL,
    CONSTRAINT PK_role PRIMARY KEY (role_id),
    CONSTRAINT UQ_role_name UNIQUE (name),
    CONSTRAINT CK_role_name CHECK (name IN ('ADMIN', 'VENDOR', 'CUSTOMER'))
);
GO

-- Table: app_user
CREATE TABLE app_user (
    user_id        INT IDENTITY(1,1) NOT NULL,
    role_id        INT               NOT NULL,
    username       NVARCHAR(50)      NOT NULL,
    email          NVARCHAR(100)     NOT NULL,
    password_hash  NVARCHAR(255)     NOT NULL,
    enabled        BIT               NOT NULL CONSTRAINT DF_app_user_enabled DEFAULT 1,
    CONSTRAINT PK_app_user PRIMARY KEY (user_id),
    CONSTRAINT UQ_app_user_username UNIQUE (username),
    CONSTRAINT UQ_app_user_email UNIQUE (email),
    CONSTRAINT FK_app_user_role FOREIGN KEY (role_id) REFERENCES role (role_id)
);
GO
-- Table: vendor
CREATE TABLE vendor (
    vendor_id     INT IDENTITY(1,1) NOT NULL,
    user_id       INT               NOT NULL,
    company_name  NVARCHAR(100)     NOT NULL,
    phone         NVARCHAR(20)      NULL,
    active        BIT               NOT NULL CONSTRAINT DF_vendor_active DEFAULT 1,
    CONSTRAINT PK_vendor PRIMARY KEY (vendor_id),
    CONSTRAINT UQ_vendor_user UNIQUE (user_id),
    CONSTRAINT UQ_vendor_company_name UNIQUE (company_name),
    CONSTRAINT FK_vendor_user FOREIGN KEY (user_id) REFERENCES app_user (user_id)
);
GO

-- Table: customer
CREATE TABLE customer (
    customer_id  INT IDENTITY(1,1) NOT NULL,
    user_id      INT               NOT NULL,
    first_name   NVARCHAR(50)      NOT NULL,
    last_name    NVARCHAR(50)      NOT NULL,
    phone        NVARCHAR(20)      NULL,
    CONSTRAINT PK_customer PRIMARY KEY (customer_id),
    CONSTRAINT UQ_customer_user UNIQUE (user_id),
    CONSTRAINT FK_customer_user FOREIGN KEY (user_id) REFERENCES app_user (user_id)
);
GO

-- Table: address
CREATE TABLE address (
    address_id   INT IDENTITY(1,1) NOT NULL,
    customer_id  INT               NOT NULL,
    street       NVARCHAR(150)     NOT NULL,
    city         NVARCHAR(80)      NOT NULL,
    province     NVARCHAR(50)      NOT NULL,
    postal_code  NVARCHAR(10)      NOT NULL,
    country      NVARCHAR(50)      NOT NULL,
    CONSTRAINT PK_address PRIMARY KEY (address_id),
    CONSTRAINT FK_address_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
);
GO

-- Table: category
CREATE TABLE category (
    category_id  INT IDENTITY(1,1) NOT NULL,
    name         NVARCHAR(50)      NOT NULL,
    description  NVARCHAR(255)     NULL,
    CONSTRAINT PK_category PRIMARY KEY (category_id),
    CONSTRAINT UQ_category_name UNIQUE (name)
);
GO

-- Table: product
CREATE TABLE product (
    product_id   INT IDENTITY(1,1) NOT NULL,
    vendor_id    INT               NOT NULL,
    category_id  INT               NOT NULL,
    sku          NVARCHAR(30)      NOT NULL,
    name         NVARCHAR(100)     NOT NULL,
    description  NVARCHAR(500)     NULL,
    price        DECIMAL(10,2)     NOT NULL,
    quantity     INT               NOT NULL,
    status       NVARCHAR(20)      NOT NULL CONSTRAINT DF_product_status DEFAULT 'IN_STOCK',
    version      INT               NOT NULL CONSTRAINT DF_product_version DEFAULT 0,
    CONSTRAINT PK_product PRIMARY KEY (product_id),
    CONSTRAINT UQ_product_sku UNIQUE (sku),
    CONSTRAINT FK_product_vendor FOREIGN KEY (vendor_id) REFERENCES vendor (vendor_id),
    CONSTRAINT FK_product_category FOREIGN KEY (category_id) REFERENCES category (category_id),
    CONSTRAINT CK_product_price CHECK (price > 0),
    CONSTRAINT CK_product_quantity CHECK (quantity >= 0),
    CONSTRAINT CK_product_status CHECK (status IN ('IN_STOCK', 'OUT_OF_STOCK'))
);
GO

-- Table: customer_order
CREATE TABLE customer_order (
    order_id     INT IDENTITY(1,1) NOT NULL,
    customer_id  INT               NOT NULL,
    address_id   INT               NOT NULL,
    order_date   DATETIME2(0)      NOT NULL CONSTRAINT DF_customer_order_date DEFAULT SYSDATETIME(),
    status       NVARCHAR(20)      NOT NULL CONSTRAINT DF_customer_order_status DEFAULT 'PAID',
    total        DECIMAL(12,2)     NOT NULL,
    CONSTRAINT PK_customer_order PRIMARY KEY (order_id),
    CONSTRAINT FK_customer_order_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT FK_customer_order_address FOREIGN KEY (address_id) REFERENCES address (address_id),
    CONSTRAINT CK_customer_order_status CHECK (status IN ('PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT CK_customer_order_total CHECK (total > 0)
);
GO

-- Table: order_item
CREATE TABLE order_item (
    order_item_id  INT IDENTITY(1,1) NOT NULL,
    order_id       INT               NOT NULL,
    product_id     INT               NOT NULL,
    quantity       INT               NOT NULL,
    unit_price     DECIMAL(10,2)     NOT NULL,
    CONSTRAINT PK_order_item PRIMARY KEY (order_item_id),
    CONSTRAINT UQ_order_item_order_product UNIQUE (order_id, product_id),
    CONSTRAINT FK_order_item_order FOREIGN KEY (order_id) REFERENCES customer_order (order_id),
    CONSTRAINT FK_order_item_product FOREIGN KEY (product_id) REFERENCES product (product_id),
    CONSTRAINT CK_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT CK_order_item_unit_price CHECK (unit_price > 0)
);
GO

-- Table: invoice
CREATE TABLE invoice (
    invoice_id      INT IDENTITY(1,1) NOT NULL,
    order_id        INT               NOT NULL,
    invoice_number  NVARCHAR(30)      NOT NULL,
    issue_date      DATETIME2(0)      NOT NULL CONSTRAINT DF_invoice_issue_date DEFAULT SYSDATETIME(),
    total           DECIMAL(12,2)     NOT NULL,
    CONSTRAINT PK_invoice PRIMARY KEY (invoice_id),
    CONSTRAINT UQ_invoice_order UNIQUE (order_id),
    CONSTRAINT UQ_invoice_number UNIQUE (invoice_number),
    CONSTRAINT FK_invoice_order FOREIGN KEY (order_id) REFERENCES customer_order (order_id),
    CONSTRAINT CK_invoice_total CHECK (total > 0)
);
GO

-- Table: payment
CREATE TABLE payment (
    payment_id      INT IDENTITY(1,1) NOT NULL,
    order_id        INT               NOT NULL,
    payment_method  NVARCHAR(20)      NOT NULL,
    amount          DECIMAL(12,2)     NOT NULL,
    payment_date    DATETIME2(0)      NOT NULL CONSTRAINT DF_payment_date DEFAULT SYSDATETIME(),
    CONSTRAINT PK_payment PRIMARY KEY (payment_id),
    CONSTRAINT FK_payment_order FOREIGN KEY (order_id) REFERENCES customer_order (order_id),
    CONSTRAINT CK_payment_method CHECK (payment_method IN ('CREDIT_CARD', 'BANK_TRANSFER')),
    CONSTRAINT CK_payment_amount CHECK (amount > 0)
);
GO