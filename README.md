# 📦 Hệ thống Quản lý Nhập – Xuất – Tồn Kho

> **Inventory Management System ** — Spring Boot 3 · Design Patterns · OOP Refactored

---

## 📋 Mục lục

- [Mô tả dự án](#-mô-tả-dự-án)
- [Design Patterns áp dụng](#-design-patterns-áp-dụng)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Chức năng hệ thống](#-chức-năng-hệ-thống)
- [Hướng dẫn cài đặt](#-hướng-dẫn-cài-đặt)
- [Tài khoản demo](#-tài-khoản-demo)

---

## 🎯 Mô tả dự án
Dự án là một hệ thống quản lý nhập – xuất – tồn kho cho một doanh nghiệp bán lẻ. Phiên bản v2 này được refactor từ v1 bằng cách áp dụng 5 design patterns quan trọng: Observer, Strategy, Factory Method, Decorator, và Facade.

- 🔧 Tăng tính **bảo trì** — thêm hành vi mới không cần sửa code cũ
- 🔌 Giảm **coupling** — các thành phần phụ thuộc vào interface, không phụ thuộc implementation
- ♻️ Tái sử dụng — logic pricing, factory, observer dùng lại cho mọi loại order
- 📖 Dễ đọc — mỗi pattern có tên rõ ràng, đúng chuẩn GoF

---

## 🔄 So sánh v1 → v2

| Khía cạnh | v1 (OOP cơ bản) | v2 (+ Design Patterns) |
|---|---|---|
| Tạo phiếu nhập/xuất | Logic trực tiếp trong Service | **Factory Method** — `ImportOrderFactory`, `ExportOrderFactory` |
| Tính giá xuất hàng | Cố định, không theo khách hàng | **Strategy** — `RetailPricing`, `WholesalePricing`, `VipPricing` |
| Ghi StockMovement | Service gọi trực tiếp repository | **Observer** — `StockEventPublisher` → các observer tự xử lý |
| Tìm kiếm sản phẩm | Gọi thẳng repository | **Decorator** — thêm Logging + Cache 60s không sửa logic gốc |
| Controller → Service | Gọi nhiều service riêng lẻ | **Facade** — `InventoryFacade` làm điểm giao tiếp duy nhất |
| Cảnh báo tồn kho thấp | Không có | `LowStockAlertObserver` tự động log cảnh báo |
| Số Java files | 58 | 78 (+20 pattern files) |

---

## 🎨 Design Patterns áp dụng

### 1. 👁️ Observer Pattern — Stock Event System

**Vị trí**: `pattern/observer/`

**Vấn đề giải quyết**: Khi tồn kho thay đổi, cần đồng thời: (1) lưu `StockMovement` vào DB, (2) phát cảnh báo nếu hàng sắp hết. Nếu viết thẳng trong service → vi phạm SRP, khó thêm hành vi mới.

**Cách hoạt động**:
```
ImportServiceImpl / ExportServiceImpl
    └─► StockEventPublisher.publish(StockEvent)
            ├─► LowStockAlertObserver.onStockChanged()    → log cảnh báo
            └─► StockMovementAuditObserver.onStockChanged() → lưu DB
```

**Lợi ích**: Thêm hành vi mới (gửi email, push notification...) chỉ cần tạo thêm 1 Observer class và `@PostConstruct register()` — **không sửa bất kỳ code nào** đang có.

| Class | Vai trò |
|---|---|
| `StockEventPublisher` | Subject — quản lý danh sách observer, publish event |
| `StockEventObserver` | Observer interface |
| `StockEvent` | Event object — đóng gói thông tin biến động |
| `LowStockAlertObserver` | Concrete Observer — log cảnh báo LOW/OUT of stock |
| `StockMovementAuditObserver` | Concrete Observer — lưu `StockMovement` vào DB |

---

### 2. 🎯 Strategy Pattern — Pricing

**Vị trí**: `pattern/strategy/`

**Vấn đề giải quyết**: Giá bán khác nhau theo loại khách hàng. Nếu dùng `if-else` trong service → code cứng nhắc, vi phạm OCP, khó thêm loại giá mới.

**Các chiến lược**:
```
PricingContext.resolveStrategy(customer)
    ├── "RETAIL"    → RetailPricingStrategy    → 0% discount
    ├── "WHOLESALE" → WholesalePricingStrategy → 5–15% (theo số lượng)
    └── "VIP"       → VipPricingStrategy       → 20–30% (cố định + loyalty)
```

**Bảng chiết khấu**:

| Loại khách | Điều kiện | Chiết khấu |
|---|---|---|
| Bán lẻ | — | 0% |
| Bán sỉ | qty ≥ 10 | 5% |
| Bán sỉ | qty ≥ 50 | 10% |
| Bán sỉ | qty ≥ 100 | 15% |
| VIP | Cơ bản | 20% |
| VIP | totalPurchase ≥ 100M | 25% |
| VIP | totalPurchase ≥ 200M | 30% |

**Tích hợp**: `ExportOrderFactory` gọi `PricingContext.getDiscountPercent()` khi tạo từng `ExportDetail`.

---

### 3. 🏭 Factory Method Pattern — Order Creation

**Vị trí**: `pattern/factory/`

**Vấn đề giải quyết**: Khởi tạo `ImportOrder`/`ExportOrder` cùng với danh sách detail và tính tổng tiền là logic phức tạp, lặp lại ở nhiều nơi. Factory tập trung logic này vào một chỗ.

```java
// Trước v2: logic rải rác trong controller và service
// Sau v2: gọi 1 dòng duy nhất
ImportOrder order = importOrderFactory.createOrder(request);
ExportOrder order = exportOrderFactory.createOrder(request);
```

**`OrderRequest`** — DTO chứa tất cả thông tin đầu vào (supplierId, customerId, danh sách sản phẩm, giá...).

**`ExportOrderFactory`** kết hợp với **Strategy**: khi build từng `ExportDetail`, tự động gọi `PricingContext` để điền `discountPercent` đúng theo loại khách hàng.

---

### 4. 🎀 Decorator Pattern — Product Search

**Vị trí**: `pattern/proxy/`

**Vấn đề giải quyết**: Cần thêm logging thời gian tìm kiếm và cache kết quả mà **không sửa** `BaseProductSearchService`.

**Decorator chain**:
```
ProductController
    └─► CachingProductSearchDecorator   (Cache 60s, LRU 50 entries)
            └─► LoggingProductSearchDecorator  (Log thời gian, cảnh báo > 1s)
                    └─► BaseProductSearchService  (Gọi thẳng repository)
```

**Cơ chế cache**:
- Key: `keyword|page|size`
- TTL: 60 giây
- Kích thước tối đa: 50 entries (LRU eviction)
- Tự động `invalidate()` khi sản phẩm được thêm/sửa/xóa

---

### 5. 🏛️ Facade Pattern — InventoryFacade

**Vị trí**: `pattern/facade/`

**Vấn đề giải quyết**: Controller phải biết và phối hợp nhiều service/factory/observer → tight coupling, khó test, khó thay đổi.

**Trước Facade**:
```java
// Controller phải biết 5+ dependencies
importOrderFactory.createOrder(request);
importOrderRepository.save(order);
productRepository.findById(id);
product.increaseStock(qty);
stockEventPublisher.publish(event);
```

**Sau Facade**:
```java
// Controller chỉ cần 1 dòng
inventoryFacade.confirmImport(orderId);
// Facade tự phối hợp: Factory → Observer → Strategy
```

---

## 🛠 Công nghệ sử dụng

*(Giống v1 — xem README_v1.md)*

| Công nghệ | Phiên bản |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.x |
| Spring Security | 6.x |
| SQL Server | 2019+ |
| Thymeleaf + Bootstrap 5 | 3.1.x + 5.3 |
| Chart.js | 4.x |
| Lombok | 1.18.x |

---

## 📁 Cấu trúc dự án

```
src/main/java/com/inventory/
├── InventoryApplication.java
├── config/
├── controller/                      # 7 controllers (dùng InventoryFacade)
├── dto/
├── entity/base/                     # BaseEntity, AbstractOrder
├── entity/                          # 12 entity classes
├── enums/                           # 5 enums
├── exception/                       # BusinessException hierarchy
├── repository/                      # 9 repositories
├── service/                         # 7 interfaces
├── service/impl/                    # 7 implementations (dùng patterns)
│
└── pattern/                         # ★ MỚI TRONG v2
    ├── observer/
    │   ├── StockEvent.java              # Event object (Value Object)
    │   ├── StockEventObserver.java      # Observer interface
    │   ├── StockEventPublisher.java     # Subject (Spring bean)
    │   ├── LowStockAlertObserver.java   # Concrete Observer: alert
    │   └── StockMovementAuditObserver.java  # Concrete Observer: audit DB
    │
    ├── strategy/
    │   ├── PricingStrategy.java         # Strategy interface
    │   ├── RetailPricingStrategy.java   # 0% discount
    │   ├── WholesalePricingStrategy.java # 5–15% by quantity
    │   ├── VipPricingStrategy.java      # 20–30% VIP + loyalty
    │   └── PricingContext.java          # Context (chọn strategy)
    │
    ├── factory/
    │   ├── OrderFactory.java            # Creator interface
    │   ├── OrderRequest.java            # Input DTO
    │   ├── ImportOrderFactory.java      # Concrete Creator
    │   └── ExportOrderFactory.java      # Concrete Creator (+ Strategy)
    │
    ├── proxy/
    │   ├── ProductSearchService.java    # Component interface
    │   ├── BaseProductSearchService.java     # Concrete Component
    │   ├── ProductSearchDecorator.java       # Abstract Decorator
    │   ├── LoggingProductSearchDecorator.java # Logs time + slow query
    │   └── CachingProductSearchDecorator.java # In-memory LRU cache
    │
    └── facade/
        └── InventoryFacade.java         # Facade: điểm giao tiếp duy nhất
```

---

## ✅ Chức năng hệ thống

### Tất cả chức năng của v1, cộng thêm:

#### 💰 Chiết khấu thông minh (Strategy)
- Tự động áp dụng chiết khấu khi tạo phiếu xuất theo loại khách hàng
- Bán lẻ: giá niêm yết → Bán sỉ: giảm theo số lượng → VIP: giảm theo loyalty

#### 🔔 Cảnh báo tồn kho real-time (Observer)
- Log tự động khi sản phẩm chuyển sang LOW_STOCK
- Log tự động khi sản phẩm chuyển sang OUT_OF_STOCK
- Có thể mở rộng: thêm email/SMS observer không cần sửa code

#### ⚡ Tìm kiếm sản phẩm nhanh hơn (Decorator)
- Cache kết quả tìm kiếm 60 giây
- Log thời gian query, cảnh báo query chậm > 1 giây
- Cache tự động xóa khi có thay đổi dữ liệu

#### 🏗️ Khởi tạo phiếu chuẩn hóa (Factory Method)
- Mọi phiếu nhập/xuất đều qua Factory — đảm bảo format code, tính tổng tiền nhất quán

---

## 🚀 Hướng dẫn cài đặt

### Bước 1 — Tạo Database

```sql
-- Chạy file db.sql trong SSMS
-- Database: inventory_db
```

### Bước 2 — Cấu hình

```properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=inventory_db;encrypt=false;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourPassword123
```

### Bước 3 — Chạy

```bash
mvn clean install
mvn spring-boot:run
# Truy cập: http://localhost:8080
```

---

## 🔑 Tài khoản demo

```sql
UPDATE app_users
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE username IN ('admin', 'manager', 'staff1');
```

| Tài khoản | Mật khẩu | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `manager` | `admin123` | MANAGER |
| `staff1` | `admin123` | STAFF |

---

## 📖 Tóm tắt Design Patterns

```
┌──────────────────────────────────────────────────────────────────┐
│                        REQUEST FLOW (v2)                         │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Browser  ──►  Controller  ──►  InventoryFacade  (FACADE)       │
│                                       │                          │
│                          ┌────────────┤                          │
│                          │            │                          │
│                   ImportOrderFactory  ExportOrderFactory         │
│                   (FACTORY METHOD)    (FACTORY METHOD)           │
│                          │            │                          │
│                          │     PricingContext (STRATEGY)         │
│                          │     ├── RetailPricingStrategy         │
│                          │     ├── WholesalePricingStrategy      │
│                          │     └── VipPricingStrategy            │
│                          │                                       │
│                   StockEventPublisher (OBSERVER - Subject)       │
│                   ├── LowStockAlertObserver    → log alert       │
│                   └── StockMovementAuditObserver → save DB       │
│                                                                  │
│  ProductSearch:  CachingDecorator → LoggingDecorator → Base     │
│                  (DECORATOR CHAIN)                               │
└──────────────────────────────────────────────────────────────────┘
```
