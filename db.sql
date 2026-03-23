-- ============================================================
--  HỆ THỐNG QUẢN LÝ NHẬP – XUẤT – TỒN KHO 
--  SQL Server Script: Tạo bảng nâng cấp + Dữ liệu mẫu
--  Database: inventory_db
-- ============================================================

USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'inventory_db')
BEGIN
    CREATE DATABASE inventory_db COLLATE Vietnamese_CI_AS;
    PRINT '>> Database inventory_db created.';
END
GO

USE inventory_db;
GO

-- ============================================================
-- DROP TABLES (reverse dependency order)
-- ============================================================
IF OBJECT_ID('stock_movements',   'U') IS NOT NULL DROP TABLE stock_movements;
IF OBJECT_ID('export_details',    'U') IS NOT NULL DROP TABLE export_details;
IF OBJECT_ID('import_details',    'U') IS NOT NULL DROP TABLE import_details;
IF OBJECT_ID('export_orders',     'U') IS NOT NULL DROP TABLE export_orders;
IF OBJECT_ID('import_orders',     'U') IS NOT NULL DROP TABLE import_orders;
IF OBJECT_ID('products',          'U') IS NOT NULL DROP TABLE products;
IF OBJECT_ID('customers',         'U') IS NOT NULL DROP TABLE customers;
IF OBJECT_ID('suppliers',         'U') IS NOT NULL DROP TABLE suppliers;
IF OBJECT_ID('categories',        'U') IS NOT NULL DROP TABLE categories;
IF OBJECT_ID('units',             'U') IS NOT NULL DROP TABLE units;
IF OBJECT_ID('warehouses',        'U') IS NOT NULL DROP TABLE warehouses;
IF OBJECT_ID('app_users',         'U') IS NOT NULL DROP TABLE app_users;
GO

-- ============================================================
-- 1. APP USERS (Quản lý người dùng)
-- ============================================================
CREATE TABLE app_users (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    username     VARCHAR(50)    NOT NULL UNIQUE,
    password     VARCHAR(255)   NOT NULL,
    full_name    NVARCHAR(150)  NOT NULL,
    email        VARCHAR(100)   NOT NULL,
    phone        VARCHAR(20)    NULL,
    role         VARCHAR(20)    NOT NULL DEFAULT 'STAFF',  -- ADMIN | MANAGER | STAFF
    status       BIT            NOT NULL DEFAULT 1,
    last_login   DATETIME       NULL,
    -- Audit
    created_at   DATETIME       NOT NULL DEFAULT GETDATE(),
    updated_at   DATETIME       NOT NULL DEFAULT GETDATE(),
    created_by   VARCHAR(50)    NOT NULL DEFAULT 'system',
    updated_by   VARCHAR(50)    NOT NULL DEFAULT 'system',
    deleted      BIT            NOT NULL DEFAULT 0,

    CONSTRAINT UQ_user_username UNIQUE (username),
    CONSTRAINT CK_user_role     CHECK (role IN ('ADMIN','MANAGER','STAFF'))
);
GO

-- ============================================================
-- 2. UNITS (Đơn vị tính)
-- ============================================================
CREATE TABLE units (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    name         NVARCHAR(50)   NOT NULL UNIQUE,
    abbreviation NVARCHAR(20)   NULL,
    description  NVARCHAR(255)  NULL,
    -- Audit
    created_at   DATETIME       NOT NULL DEFAULT GETDATE(),
    updated_at   DATETIME       NOT NULL DEFAULT GETDATE(),
    created_by   VARCHAR(50)    NOT NULL DEFAULT 'system',
    updated_by   VARCHAR(50)    NOT NULL DEFAULT 'system',
    deleted      BIT            NOT NULL DEFAULT 0
);
GO

-- ============================================================
-- 3. WAREHOUSES (Kho hàng)
-- ============================================================
CREATE TABLE warehouses (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    code         VARCHAR(20)    NOT NULL UNIQUE,
    name         NVARCHAR(150)  NOT NULL,
    address      NVARCHAR(300)  NULL,
    capacity     INT            NULL DEFAULT 0,
    manager_name NVARCHAR(100)  NULL,
    phone        VARCHAR(20)    NULL,
    status       BIT            NOT NULL DEFAULT 1,
    -- Audit
    created_at   DATETIME       NOT NULL DEFAULT GETDATE(),
    updated_at   DATETIME       NOT NULL DEFAULT GETDATE(),
    created_by   VARCHAR(50)    NOT NULL DEFAULT 'system',
    updated_by   VARCHAR(50)    NOT NULL DEFAULT 'system',
    deleted      BIT            NOT NULL DEFAULT 0
);
GO

-- ============================================================
-- 4. CATEGORIES (Danh mục – hỗ trợ đệ quy cha/con)
-- ============================================================
CREATE TABLE categories (
    id                 BIGINT IDENTITY(1,1) PRIMARY KEY,
    name               NVARCHAR(100)  NOT NULL,
    description        NVARCHAR(255)  NULL,
    parent_category_id BIGINT         NULL,
    sort_order         INT            NOT NULL DEFAULT 0,
    -- Audit
    created_at         DATETIME       NOT NULL DEFAULT GETDATE(),
    updated_at         DATETIME       NOT NULL DEFAULT GETDATE(),
    created_by         VARCHAR(50)    NOT NULL DEFAULT 'system',
    updated_by         VARCHAR(50)    NOT NULL DEFAULT 'system',
    deleted            BIT            NOT NULL DEFAULT 0,

    CONSTRAINT FK_category_parent FOREIGN KEY (parent_category_id) REFERENCES categories(id)
);
GO

-- ============================================================
-- 5. SUPPLIERS (Nhà cung cấp – nâng cấp thêm thông tin tài chính)
-- ============================================================
CREATE TABLE suppliers (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    code           VARCHAR(20)    NOT NULL UNIQUE,
    name           NVARCHAR(150)  NOT NULL,
    contact_person NVARCHAR(100)  NULL,
    phone          VARCHAR(20)    NULL,
    email          VARCHAR(100)   NULL,
    address        NVARCHAR(300)  NULL,
    tax_code       VARCHAR(20)    NULL,
    bank_account   VARCHAR(30)    NULL,
    bank_name      NVARCHAR(100)  NULL,
    credit_limit   DECIMAL(18,2)  NOT NULL DEFAULT 0,
    current_debt   DECIMAL(18,2)  NOT NULL DEFAULT 0,
    status         BIT            NOT NULL DEFAULT 1,
    -- Audit
    created_at     DATETIME       NOT NULL DEFAULT GETDATE(),
    updated_at     DATETIME       NOT NULL DEFAULT GETDATE(),
    created_by     VARCHAR(50)    NOT NULL DEFAULT 'system',
    updated_by     VARCHAR(50)    NOT NULL DEFAULT 'system',
    deleted        BIT            NOT NULL DEFAULT 0,

    CONSTRAINT UQ_supplier_code UNIQUE (code)
);
GO

-- ============================================================
-- 6. CUSTOMERS (Khách hàng – tách riêng khỏi export_orders)
-- ============================================================
CREATE TABLE customers (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    code           VARCHAR(20)    NOT NULL UNIQUE,
    name           NVARCHAR(150)  NOT NULL,
    phone          VARCHAR(20)    NULL,
    email          VARCHAR(100)   NULL,
    address        NVARCHAR(300)  NULL,
    tax_code       VARCHAR(20)    NULL,
    customer_type  VARCHAR(20)    NOT NULL DEFAULT 'RETAIL',  -- RETAIL | WHOLESALE | VIP
    total_purchase DECIMAL(18,2)  NOT NULL DEFAULT 0,
    status         BIT            NOT NULL DEFAULT 1,
    -- Audit
    created_at     DATETIME       NOT NULL DEFAULT GETDATE(),
    updated_at     DATETIME       NOT NULL DEFAULT GETDATE(),
    created_by     VARCHAR(50)    NOT NULL DEFAULT 'system',
    updated_by     VARCHAR(50)    NOT NULL DEFAULT 'system',
    deleted        BIT            NOT NULL DEFAULT 0,

    CONSTRAINT UQ_customer_code  UNIQUE (code),
    CONSTRAINT CK_customer_type  CHECK (customer_type IN ('RETAIL','WHOLESALE','VIP'))
);
GO

-- ============================================================
-- 7. PRODUCTS (Sản phẩm – nâng cấp đầy đủ)
-- ============================================================
CREATE TABLE products (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    code          VARCHAR(20)     NOT NULL UNIQUE,
    name          NVARCHAR(200)   NOT NULL,
    barcode       VARCHAR(50)     NULL UNIQUE,
    category_id   BIGINT          NULL,
    supplier_id   BIGINT          NULL,
    unit_id       BIGINT          NULL,
    warehouse_id  BIGINT          NULL,
    cost_price    DECIMAL(18,2)   NOT NULL DEFAULT 0,
    sell_price    DECIMAL(18,2)   NOT NULL DEFAULT 0,
    quantity      INT             NOT NULL DEFAULT 0,
    min_quantity  INT             NOT NULL DEFAULT 5,
    max_quantity  INT             NULL DEFAULT 999999,
    weight        DECIMAL(10,3)   NULL,
    description   NVARCHAR(500)   NULL,
    image_url     NVARCHAR(500)   NULL,
    status        BIT             NOT NULL DEFAULT 1,
    -- Audit
    created_at    DATETIME        NOT NULL DEFAULT GETDATE(),
    updated_at    DATETIME        NOT NULL DEFAULT GETDATE(),
    created_by    VARCHAR(50)     NOT NULL DEFAULT 'system',
    updated_by    VARCHAR(50)     NOT NULL DEFAULT 'system',
    deleted       BIT             NOT NULL DEFAULT 0,

    CONSTRAINT FK_product_category  FOREIGN KEY (category_id)  REFERENCES categories(id),
    CONSTRAINT FK_product_supplier  FOREIGN KEY (supplier_id)  REFERENCES suppliers(id),
    CONSTRAINT FK_product_unit      FOREIGN KEY (unit_id)      REFERENCES units(id),
    CONSTRAINT FK_product_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT UQ_product_code      UNIQUE (code),
    CONSTRAINT CK_product_cost      CHECK (cost_price >= 0),
    CONSTRAINT CK_product_sell      CHECK (sell_price >= 0),
    CONSTRAINT CK_product_qty       CHECK (quantity   >= 0),
    CONSTRAINT CK_product_minqty    CHECK (min_quantity >= 0)
);
GO

-- ============================================================
-- 8. IMPORT ORDERS (Phiếu nhập – nâng cấp)
-- ============================================================
CREATE TABLE import_orders (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    code             VARCHAR(20)    NOT NULL UNIQUE,
    order_date       DATE           NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    expected_date    DATE           NULL,
    received_date    DATE           NULL,
    supplier_id      BIGINT         NULL,
    invoice_number   VARCHAR(50)    NULL,
    total_amount     DECIMAL(18,2)  NOT NULL DEFAULT 0,
    discount_amount  DECIMAL(18,2)  NOT NULL DEFAULT 0,
    tax_amount       DECIMAL(18,2)  NOT NULL DEFAULT 0,
    final_amount     DECIMAL(18,2)  NOT NULL DEFAULT 0,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    payment_status   VARCHAR(20)    NOT NULL DEFAULT 'UNPAID',
    note             NVARCHAR(500)  NULL,
    -- Audit
    created_at       DATETIME       NOT NULL DEFAULT GETDATE(),
    updated_at       DATETIME       NOT NULL DEFAULT GETDATE(),
    created_by       VARCHAR(50)    NOT NULL DEFAULT 'system',
    updated_by       VARCHAR(50)    NOT NULL DEFAULT 'system',
    deleted          BIT            NOT NULL DEFAULT 0,

    CONSTRAINT FK_import_supplier   FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT UQ_import_code       UNIQUE (code),
    CONSTRAINT CK_import_status     CHECK (status         IN ('PENDING','COMPLETED','CANCELLED')),
    CONSTRAINT CK_import_pay_status CHECK (payment_status IN ('UNPAID','PARTIAL','PAID'))
);
GO

-- ============================================================
-- 9. IMPORT DETAILS (Chi tiết phiếu nhập – nâng cấp)
-- ============================================================
CREATE TABLE import_details (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    import_order_id  BIGINT         NOT NULL,
    product_id       BIGINT         NOT NULL,
    quantity         INT            NOT NULL,
    unit_price       DECIMAL(18,2)  NOT NULL,
    total_price      AS (CAST(quantity AS DECIMAL(18,2)) * unit_price) PERSISTED,
    expiry_date      DATE           NULL,
    batch_number     VARCHAR(50)    NULL,
    -- Audit
    created_at       DATETIME       NOT NULL DEFAULT GETDATE(),
    updated_at       DATETIME       NOT NULL DEFAULT GETDATE(),
    created_by       VARCHAR(50)    NOT NULL DEFAULT 'system',
    updated_by       VARCHAR(50)    NOT NULL DEFAULT 'system',
    deleted          BIT            NOT NULL DEFAULT 0,

    CONSTRAINT FK_imp_det_order   FOREIGN KEY (import_order_id) REFERENCES import_orders(id) ON DELETE CASCADE,
    CONSTRAINT FK_imp_det_product FOREIGN KEY (product_id)      REFERENCES products(id),
    CONSTRAINT CK_imp_det_qty     CHECK (quantity   > 0),
    CONSTRAINT CK_imp_det_price   CHECK (unit_price >= 0)
);
GO

-- ============================================================
-- 10. EXPORT ORDERS (Phiếu xuất – nâng cấp dùng Customer)
-- ============================================================
CREATE TABLE export_orders (
    id                BIGINT IDENTITY(1,1) PRIMARY KEY,
    code              VARCHAR(20)    NOT NULL UNIQUE,
    order_date        DATE           NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    expected_delivery DATE           NULL,
    actual_delivery   DATE           NULL,
    customer_id       BIGINT         NULL,
    delivery_address  NVARCHAR(300)  NULL,
    total_amount      DECIMAL(18,2)  NOT NULL DEFAULT 0,
    discount_amount   DECIMAL(18,2)  NOT NULL DEFAULT 0,
    tax_amount        DECIMAL(18,2)  NOT NULL DEFAULT 0,
    final_amount      DECIMAL(18,2)  NOT NULL DEFAULT 0,
    status            VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    payment_status    VARCHAR(20)    NOT NULL DEFAULT 'UNPAID',
    note              NVARCHAR(500)  NULL,
    -- Audit
    created_at        DATETIME       NOT NULL DEFAULT GETDATE(),
    updated_at        DATETIME       NOT NULL DEFAULT GETDATE(),
    created_by        VARCHAR(50)    NOT NULL DEFAULT 'system',
    updated_by        VARCHAR(50)    NOT NULL DEFAULT 'system',
    deleted           BIT            NOT NULL DEFAULT 0,

    CONSTRAINT FK_export_customer   FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT UQ_export_code       UNIQUE (code),
    CONSTRAINT CK_export_status     CHECK (status         IN ('PENDING','COMPLETED','CANCELLED')),
    CONSTRAINT CK_export_pay_status CHECK (payment_status IN ('UNPAID','PARTIAL','PAID'))
);
GO

-- ============================================================
-- 11. EXPORT DETAILS (Chi tiết phiếu xuất – thêm discount)
-- ============================================================
CREATE TABLE export_details (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    export_order_id  BIGINT         NOT NULL,
    product_id       BIGINT         NOT NULL,
    quantity         INT            NOT NULL,
    unit_price       DECIMAL(18,2)  NOT NULL,
    discount_percent DECIMAL(5,2)   NOT NULL DEFAULT 0,
    total_price      AS (CAST(quantity AS DECIMAL(18,2)) * unit_price * (1 - discount_percent/100)) PERSISTED,
    -- Audit
    created_at       DATETIME       NOT NULL DEFAULT GETDATE(),
    updated_at       DATETIME       NOT NULL DEFAULT GETDATE(),
    created_by       VARCHAR(50)    NOT NULL DEFAULT 'system',
    updated_by       VARCHAR(50)    NOT NULL DEFAULT 'system',
    deleted          BIT            NOT NULL DEFAULT 0,

    CONSTRAINT FK_exp_det_order   FOREIGN KEY (export_order_id) REFERENCES export_orders(id) ON DELETE CASCADE,
    CONSTRAINT FK_exp_det_product FOREIGN KEY (product_id)      REFERENCES products(id),
    CONSTRAINT CK_exp_det_qty     CHECK (quantity          > 0),
    CONSTRAINT CK_exp_det_price   CHECK (unit_price       >= 0),
    CONSTRAINT CK_exp_det_disc    CHECK (discount_percent BETWEEN 0 AND 100)
);
GO

-- ============================================================
-- 12. STOCK MOVEMENTS (Lịch sử tồn kho – NEW)
-- ============================================================
CREATE TABLE stock_movements (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id       BIGINT         NOT NULL,
    warehouse_id     BIGINT         NULL,
    movement_type    VARCHAR(20)    NOT NULL,  -- IN | OUT | ADJUST | TRANSFER
    quantity         INT            NOT NULL,
    before_quantity  INT            NOT NULL,
    after_quantity   INT            NOT NULL,
    reference_code   VARCHAR(30)    NULL,      -- mã phiếu liên quan
    reference_type   VARCHAR(20)    NULL,      -- IMPORT | EXPORT | ADJUST
    note             NVARCHAR(300)  NULL,
    -- Audit
    created_at       DATETIME       NOT NULL DEFAULT GETDATE(),
    updated_at       DATETIME       NOT NULL DEFAULT GETDATE(),
    created_by       VARCHAR(50)    NOT NULL DEFAULT 'system',
    updated_by       VARCHAR(50)    NOT NULL DEFAULT 'system',
    deleted          BIT            NOT NULL DEFAULT 0,

    CONSTRAINT FK_movement_product   FOREIGN KEY (product_id)   REFERENCES products(id),
    CONSTRAINT FK_movement_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT CK_movement_type      CHECK (movement_type IN ('IN','OUT','ADJUST','TRANSFER'))
);
GO

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IX_products_code         ON products        (code)         WHERE deleted = 0;
CREATE INDEX IX_products_category     ON products        (category_id)  WHERE deleted = 0;
CREATE INDEX IX_products_status       ON products        (status)       WHERE deleted = 0;
CREATE INDEX IX_products_quantity     ON products        (quantity)     WHERE deleted = 0;
CREATE INDEX IX_import_orders_date    ON import_orders   (order_date)   WHERE deleted = 0;
CREATE INDEX IX_import_orders_status  ON import_orders   (status)       WHERE deleted = 0;
CREATE INDEX IX_export_orders_date    ON export_orders   (order_date)   WHERE deleted = 0;
CREATE INDEX IX_export_orders_status  ON export_orders   (status)       WHERE deleted = 0;
CREATE INDEX IX_stock_movements_prod  ON stock_movements (product_id)   WHERE deleted = 0;
CREATE INDEX IX_stock_movements_date  ON stock_movements (created_at);
CREATE INDEX IX_customers_code        ON customers       (code)         WHERE deleted = 0;
CREATE INDEX IX_suppliers_code        ON suppliers       (code)         WHERE deleted = 0;
GO

PRINT '>> Tables and indexes created successfully!';
GO

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- Users
INSERT INTO app_users (username, password, full_name, email, phone, role, status) VALUES
('admin',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Quản trị viên',    'admin@ims.vn',    '0901000001', 'ADMIN',   1),
('manager', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Nguyễn Quản Lý',  'manager@ims.vn',  '0901000002', 'MANAGER', 1),
('staff1',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Trần Nhân Viên',  'staff1@ims.vn',   '0901000003', 'STAFF',   1);
GO

-- Units
INSERT INTO units (name, abbreviation, description) VALUES
(N'Cái',     N'Cái',  N'Đơn vị tính theo từng cái'),
(N'Hộp',     N'Hộp',  N'Đơn vị tính theo hộp'),
(N'Ram',     N'Ram',  N'Xấp giấy 500 tờ'),
(N'Thùng',   N'Thùng',N'Thùng chứa nhiều sản phẩm'),
(N'Kg',      N'kg',   N'Đơn vị khối lượng kilogram'),
(N'Lít',     N'L',    N'Đơn vị thể tích lít'),
(N'Mét',     N'm',    N'Đơn vị chiều dài mét'),
(N'Bộ',      N'Bộ',   N'Bộ gồm nhiều phụ kiện');
GO

-- Warehouses
INSERT INTO warehouses (code, name, address, capacity, manager_name, phone, status) VALUES
('KHO001', N'Kho Chính Hà Nội',     N'123 Cầu Giấy, Hà Nội',              5000, N'Nguyễn Văn A', '0912001001', 1),
('KHO002', N'Kho Phụ TP.HCM',       N'456 Bình Thạnh, TP. Hồ Chí Minh',   3000, N'Trần Thị B',   '0912001002', 1),
('KHO003', N'Kho Đà Nẵng',          N'789 Hải Châu, Đà Nẵng',             2000, N'Lê Văn C',     '0912001003', 1);
GO

-- Categories (với cấu trúc cha/con)
INSERT INTO categories (name, description, parent_category_id, sort_order) VALUES
(N'Điện tử & Công nghệ',  N'Thiết bị điện tử, linh kiện, phụ kiện',   NULL, 1),
(N'Văn phòng phẩm',       N'Dụng cụ và thiết bị văn phòng',            NULL, 2),
(N'Thiết bị mạng',        N'Router, switch, cáp mạng, modem',          NULL, 3),
(N'Thực phẩm & Đồ uống',  N'Thực phẩm đóng gói, đồ uống',             NULL, 4),
(N'Gia dụng & Nội thất',  N'Đồ gia dụng, nội thất văn phòng',         NULL, 5);
GO
-- Sub-categories
INSERT INTO categories (name, description, parent_category_id, sort_order) VALUES
(N'Laptop & Máy tính',    N'Máy tính xách tay, máy tính bàn', 1, 1),
(N'Chuột & Bàn phím',     N'Thiết bị nhập liệu',               1, 2),
(N'Màn hình',             N'Màn hình máy tính',                 1, 3),
(N'Giấy in',              N'Các loại giấy in',                  2, 1),
(N'Bút & Mực',            N'Bút viết, mực in',                  2, 2);
GO

-- Suppliers
INSERT INTO suppliers (code, name, contact_person, phone, email, address, tax_code, credit_limit, status) VALUES
('NCC0001', N'Công ty TNHH Công nghệ ABC',    N'Nguyễn Văn A', '0901234567', 'abc@techvn.com',     N'15 Nguyễn Huệ, Q.1, TP.HCM',          '0301234567', 500000000, 1),
('NCC0002', N'Cty CP Phân phối XYZ',          N'Trần Thị B',   '0912345678', 'xyz@phanboi.vn',     N'234 Lê Lợi, Q. Hai Bà Trưng, Hà Nội', '0109876543', 300000000, 1),
('NCC0003', N'Nhà phân phối Việt Hưng',       N'Lê Văn C',     '0923456789', 'viethung@supply.vn', N'89 Trần Phú, Q. Hải Châu, Đà Nẵng',   '0400012345', 200000000, 1),
('NCC0004', N'Cty TNHH Thương mại Sao Mai',  N'Phạm Thị D',   '0934567890', 'saomai@trade.vn',   N'56 Hoàng Diệu, Q. Phú Nhuận, TP.HCM', '0310234567', 150000000, 1),
('NCC0005', N'Công ty Điện máy Bắc Kỳ',      N'Hoàng Văn E',  '0945678901', 'bky@dienmay.vn',    N'102 Cầu Giấy, Q. Cầu Giấy, Hà Nội',  '0105432109', 100000000, 0);
GO

-- Customers
INSERT INTO customers (code, name, phone, email, address, customer_type, status) VALUES
('KH0001', N'Công ty TNHH Phát Đạt',     '0901111222', 'phatdat@company.vn',  N'45 Lê Lợi, Q.1, TP.HCM',          'WHOLESALE', 1),
('KH0002', N'Trường THPT Nguyễn Du',     '0912222333', 'nguyendu@school.edu', N'123 Nguyễn Du, Q.1, TP.HCM',      'RETAIL',    1),
('KH0003', N'Văn phòng Đại diện MNO',   '0923333444', 'mno@corp.vn',         N'78 Pasteur, Q.3, TP.HCM',          'WHOLESALE', 1),
('KH0004', N'Cá nhân - Nguyễn Văn An',  '0934444555', NULL,                  N'Hà Nội',                            'RETAIL',    1),
('KH0005', N'Công ty ABC Logistics',     '0945555666', 'abc@logistics.vn',    N'300 Nguyễn Thái Học, Hà Nội',     'VIP',       1),
('KH0006', N'Siêu thị Mini Thành Công',  '0956666777', 'thanhcong@mart.vn',   N'55 Đinh Tiên Hoàng, TP.HCM',      'WHOLESALE', 1);
GO

-- Products
INSERT INTO products (code, name, barcode, category_id, supplier_id, unit_id, warehouse_id,
                      cost_price, sell_price, quantity, min_quantity, max_quantity, weight, description, status)
VALUES
('SP00001', N'Laptop Dell Inspiron 15 3520',  '8901234567890', 6, 1, 1, 1,  12000000, 15500000,  8,  3, 50, 1.850, N'Intel Core i5-1235U, RAM 8GB, SSD 256GB, 15.6 inch FHD', 1),
('SP00002', N'Chuột không dây Logitech M185', '8901234567891', 7, 1, 1, 1,    180000,   290000, 45, 10,200, 0.090, N'Kết nối USB nano, pin 12 tháng, màu đen', 1),
('SP00003', N'Bàn phím cơ Keychron K2 V2',   '8901234567892', 7, 2, 1, 1,    750000,  1150000,  3,  5, 50, 0.680, N'Switch Brown, layout 75%, tương thích Win/Mac, backlight RGB', 1),
('SP00004', N'Màn hình Dell 24" FHD IPS',    '8901234567893', 8, 1, 1, 1,   3500000,  4800000,  5,  2, 20, 3.900, N'1920x1080, 75Hz, panel IPS, viền mỏng, HDMI+VGA', 1),
('SP00005', N'Giấy in A4 70gsm Double A',    '8901234567894', 9, 3, 3, 2,     48000,    62000,150, 20,500, 2.300, N'500 tờ/ram, dùng cho máy in laser & phun mực', 1),
('SP00006', N'Bộ bút bi Thiên Long TL-027',  '8901234567895',10, 3, 2, 2,     18000,    28000, 80, 15,300, 0.120, N'12 cây/hộp, mực xanh, ngòi 0.8mm', 1),
('SP00007', N'Router Wi-Fi TP-Link AX1800',  '8901234567896', 3, 4, 1, 1,    650000,   890000, 12,  4, 60, 0.350, N'Wi-Fi 6, băng tần kép 2.4GHz + 5GHz, 4 anten', 1),
('SP00008', N'Switch Mạng 8 Cổng D-Link',   '8901234567897', 3, 4, 1, 1,    280000,   420000,  2,  5, 30, 0.500, N'10/100Mbps, plug & play, 8 cổng RJ45', 1),
('SP00009', N'Nước suối Aquafina 500ml',     '8901234567898', 4, 5, 4, 2,     65000,    90000, 40,  8,100, 12.000,N'24 chai/thùng, nước tinh khiết đóng chai', 1),
('SP00010', N'Ghế xoay văn phòng HB-665',   '8901234567899', 5, 2, 1, 3,    850000,  1250000,  6,  2, 20, 12.500,N'Lưng lưới, có tựa đầu, điều chỉnh độ cao', 1),
('SP00011', N'Máy in HP LaserJet M110w',     '8901234567900', 1, 1, 1, 1,   2800000,  3900000,  4,  2, 15, 4.200, N'In laser đen trắng, Wi-Fi, tốc độ 21 trang/phút', 1),
('SP00012', N'Tai nghe Sony WH-CH520',       '8901234567901', 1, 1, 1, 1,    650000,   950000, 15,  5, 80, 0.147, N'Bluetooth 5.2, pin 50 giờ, có micro, màu trắng/đen', 1);
GO

-- Import Orders
INSERT INTO import_orders (code, order_date, supplier_id, invoice_number, total_amount, final_amount, status, payment_status, note) VALUES
('PN000001', '2024-11-05', 1, 'INV-ABC-001', 98700000, 98700000, 'COMPLETED', 'PAID',   N'Nhập hàng điện tử tháng 11 từ ABC Tech'),
('PN000002', '2024-11-18', 3, 'INV-VH-001',  16410000, 16410000, 'COMPLETED', 'PAID',   N'Nhập văn phòng phẩm tháng 11 từ Việt Hưng'),
('PN000003', '2024-12-02', 2, 'INV-XYZ-001',  6450000,  6450000, 'COMPLETED', 'PAID',   N'Nhập bàn phím cơ và thiết bị mạng'),
('PN000004', '2024-12-10', 4, 'INV-SM-001',   3340000,  3340000, 'COMPLETED', 'PARTIAL',N'Nhập router và nước suối tháng 12'),
('PN000005', '2025-01-03', 1, NULL,           15500000, 15500000, 'PENDING',   'UNPAID', N'Phiếu nhập đầu năm – chờ xác nhận'),
('PN000006', '2025-02-15', 2, 'INV-XYZ-002',  9600000,  9600000, 'COMPLETED', 'PAID',   N'Nhập thêm bàn phím và tai nghe tháng 2'),
('PN000007', '2025-03-01', 3, 'INV-VH-002',   7200000,  7200000, 'COMPLETED', 'PAID',   N'Nhập giấy A4 và bút bi tháng 3');
GO

-- Import Details
INSERT INTO import_details (import_order_id, product_id, quantity, unit_price) VALUES
(1, 1, 5, 12000000),(1, 2, 20, 180000),(1, 4, 3, 3500000),
(2, 5, 150, 48000),(2, 6, 100, 18000),
(3, 3, 5, 750000),(3, 8, 8, 280000),
(4, 7, 3, 650000),(4, 9, 20, 65000),
(5, 1, 1, 12000000),(5, 4, 1, 3500000),
(6, 3, 8, 750000),(6, 12, 10, 650000),
(7, 5, 100, 48000),(7, 6, 50, 18000);
GO

-- Export Orders
INSERT INTO export_orders (code, order_date, customer_id, delivery_address, total_amount, final_amount, status, payment_status, note) VALUES
('PX000001', '2024-11-12', 1, N'45 Lê Lợi, Q.1, TP.HCM',          20680000, 20680000, 'COMPLETED', 'PAID',    N'Xuất laptop và thiết bị cho khách lớn'),
('PX000002', '2024-11-25', 2, N'123 Nguyễn Du, Q.1, TP.HCM',       5960000,  5960000,  'COMPLETED', 'PAID',    N'Xuất văn phòng phẩm cho nhà trường'),
('PX000003', '2024-12-08', 3, N'78 Pasteur, Q.3, TP.HCM',           7200000,  7200000,  'COMPLETED', 'PAID',    N'Xuất thiết bị mạng và ghế văn phòng'),
('PX000004', '2024-12-20', 4, N'Hà Nội',                             580000,   580000,  'COMPLETED', 'PAID',    N'Bán lẻ chuột + bút bi'),
('PX000005', '2025-01-05', 5, N'300 Nguyễn Thái Học, Hà Nội',      3450000,  3450000,  'PENDING',   'UNPAID',  N'Đơn hàng đầu năm – chờ xác nhận'),
('PX000006', '2025-02-20', 6, N'55 Đinh Tiên Hoàng, TP.HCM',       4750000,  4275000,  'COMPLETED', 'PAID',    N'Xuất hàng siêu thị, chiết khấu 10%'),
('PX000007', '2025-03-10', 1, N'45 Lê Lợi, Q.1, TP.HCM',           9500000,  9500000,  'PENDING',   'UNPAID',  N'Đơn hàng tháng 3 từ Phát Đạt');
GO

-- Export Details
INSERT INTO export_details (export_order_id, product_id, quantity, unit_price, discount_percent) VALUES
(1, 1, 1, 15500000, 0),(1, 4, 1, 4800000, 0),(1, 2, 2, 290000, 0),
(2, 5, 80, 62000, 0),(2, 6, 50, 28000, 0),
(3, 7, 2, 890000, 0),(3, 8, 3, 420000, 0),(3, 10, 3, 1250000, 0),
(4, 2, 1, 290000, 0),(4, 6, 5, 28000, 0),
(5, 9, 30, 90000, 0),(5, 5, 20, 62000, 0),
(6, 5, 50, 62000, 10),(6, 6, 25, 28000, 10),(6, 2, 5, 290000, 10),
(7, 11, 2, 3900000, 0),(7, 12, 5, 950000, 0);
GO

-- Stock Movements (tự động ghi lại lịch sử)
INSERT INTO stock_movements (product_id, warehouse_id, movement_type, quantity, before_quantity, after_quantity, reference_code, reference_type, note) VALUES
(1, 1, 'IN',  5, 3,  8, 'PN000001', 'IMPORT', N'Nhập theo phiếu PN000001'),
(2, 1, 'IN', 20, 25, 45,'PN000001', 'IMPORT', N'Nhập theo phiếu PN000001'),
(4, 1, 'IN',  3, 2,  5, 'PN000001', 'IMPORT', N'Nhập theo phiếu PN000001'),
(5, 2, 'IN',150, 0, 150,'PN000002', 'IMPORT', N'Nhập theo phiếu PN000002'),
(6, 2, 'IN',100, 0, 100,'PN000002', 'IMPORT', N'Nhập theo phiếu PN000002'),
(1, 1, 'OUT', 1, 8,  7, 'PX000001', 'EXPORT', N'Xuất theo phiếu PX000001'),
(4, 1, 'OUT', 1, 5,  4, 'PX000001', 'EXPORT', N'Xuất theo phiếu PX000001'),
(2, 1, 'OUT', 2, 45, 43,'PX000001', 'EXPORT', N'Xuất theo phiếu PX000001'),
(5, 2, 'OUT',80, 150,70,'PX000002', 'EXPORT', N'Xuất theo phiếu PX000002'),
(6, 2, 'OUT',50, 100,50,'PX000002', 'EXPORT', N'Xuất theo phiếu PX000002');
GO

PRINT '>> Sample data inserted successfully!';
GO

-- ============================================================
-- VIEWS HỮU ÍCH
-- ============================================================
CREATE OR ALTER VIEW v_inventory_summary AS
SELECT
    p.id, p.code, p.name AS product_name,
    c.name  AS category,
    s.name  AS supplier,
    u.name  AS unit,
    w.name  AS warehouse,
    p.quantity AS current_stock,
    p.min_quantity, p.max_quantity,
    p.cost_price, p.sell_price,
    (p.quantity * p.cost_price) AS stock_value,
    CASE
        WHEN p.quantity = 0                    THEN N'Hết hàng'
        WHEN p.quantity <= p.min_quantity      THEN N'Sắp hết'
        ELSE                                        N'Đủ hàng'
    END AS stock_status
FROM products p
LEFT JOIN categories c ON p.category_id  = c.id
LEFT JOIN suppliers  s ON p.supplier_id  = s.id
LEFT JOIN units      u ON p.unit_id      = u.id
LEFT JOIN warehouses w ON p.warehouse_id = w.id
WHERE p.status = 1 AND p.deleted = 0;
GO

CREATE OR ALTER VIEW v_monthly_import AS
SELECT
    YEAR(order_date)  AS nam,
    MONTH(order_date) AS thang,
    COUNT(*)          AS so_phieu,
    SUM(final_amount) AS tong_tien
FROM import_orders
WHERE status = 'COMPLETED' AND deleted = 0
GROUP BY YEAR(order_date), MONTH(order_date);
GO

CREATE OR ALTER VIEW v_monthly_export AS
SELECT
    YEAR(order_date)  AS nam,
    MONTH(order_date) AS thang,
    COUNT(*)          AS so_phieu,
    SUM(final_amount) AS tong_tien
FROM export_orders
WHERE status = 'COMPLETED' AND deleted = 0
GROUP BY YEAR(order_date), MONTH(order_date);
GO

CREATE OR ALTER VIEW v_stock_movements_detail AS
SELECT
    sm.id, sm.created_at, sm.movement_type,
    p.code AS product_code, p.name AS product_name,
    u.name AS unit,
    w.name AS warehouse,
    sm.quantity, sm.before_quantity, sm.after_quantity,
    sm.reference_code, sm.reference_type, sm.note,
    sm.created_by
FROM stock_movements sm
JOIN products   p ON sm.product_id   = p.id
LEFT JOIN units      u ON p.unit_id       = u.id
LEFT JOIN warehouses w ON sm.warehouse_id = w.id
WHERE sm.deleted = 0
GO

PRINT '>> Views created successfully!';
PRINT '';
PRINT 'Database inventory_db setup complete.';
PRINT 'SELECT * FROM v_inventory_summary;';
PRINT 'SELECT * FROM v_monthly_import;';
PRINT 'SELECT * FROM v_monthly_export;';
GO
