# Customer VIP Spending & Auto-Classification Implementation Guide

## Tổng quan tính năng

Hệ thống đã được cập nhật để quản lý tổng tiền VIP đã chi tiêu của khách hàng và tự động phân loại customer type dựa trên số tiền đã chi.

## 🗄️ Database Changes

### 1. Trường mới trong bảng `customer`

```sql
-- Thêm trường total_vip_spent
ALTER TABLE customer 
ADD COLUMN total_vip_spent DECIMAL(15,2) DEFAULT 0.00 NOT NULL;

-- Tạo index để tối ưu hiệu suất
CREATE INDEX idx_customer_total_vip_spent ON customer(total_vip_spent);
```

### 2. Migration Script

Chạy file migration: `start-gym-BE/src/main/resources/db/migration/V1__Add_total_vip_spent_to_customer.sql`

Script này sẽ:
- Thêm trường `total_vip_spent` vào bảng customer
- Tính toán và cập nhật giá trị từ lịch sử thanh toán có sẵn
- Tạo index để tối ưu hiệu suất

## 🔧 Backend API Endpoints

### 1. Đồng bộ VIP Spending

```http
POST /api/v1/admin/sync-customer-vip-spending
```

**Mục đích:** Tính lại `total_vip_spent` cho tất cả customers từ lịch sử thanh toán

**Response:**
```json
{
  "message": "Customer VIP spending synchronized successfully",
  "updatedCustomers": 25,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 2. Tự động phân loại Customer Types

```http
POST /api/v1/admin/auto-classify-customer-types
```

**Mục đích:** Phân loại lại customer types dựa trên `total_vip_spent`

**Response:**
```json
{
  "message": "Customer types classified successfully",
  "updatedCustomers": 12,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 3. Full Sync (Đồng bộ toàn bộ)

```http
POST /api/v1/admin/full-sync-customer-data
```

**Mục đích:** Thực hiện cả 2 bước: đồng bộ spending + phân loại types

**Response:**
```json
{
  "message": "Full customer data synchronization completed",
  "spendingSyncCount": 25,
  "typeUpdateCount": 12,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 4. Thống kê VIP Spending

```http
GET /api/v1/admin/customer-vip-spending-stats
```

**Response:**
```json
{
  "totalCustomers": 100,
  "customersWithSpending": 45,
  "totalVipSpending": 150000000,
  "averageSpending": 3333333.33,
  "topSpenders": [
    {
      "customerId": 1,
      "customerName": "Nguyễn Văn A",
      "totalVipSpent": 5000000,
      "customerType": "VIP"
    }
  ],
  "typeStats": {
    "VIP": {
      "count": 10,
      "totalSpending": 75000000,
      "averageSpending": 7500000
    }
  }
}
```

## 🔄 Automatic Updates

### 1. Khi thanh toán thành công

Khi VIP payment có status = SUCCESS:
- Tự động cộng amount vào `total_vip_spent`
- Tự động kiểm tra và cập nhật customer type nếu cần

### 2. Khi hoàn tiền

Khi payment chuyển từ SUCCESS sang trạng thái khác:
- Tự động trừ amount từ `total_vip_spent`
- Tự động kiểm tra và cập nhật customer type nếu cần

### 3. Khi thay đổi Customer Type thresholds

Khi admin thay đổi giá trị price của customer types:
- (Tùy chọn) Có thể trigger automatic re-classification cho toàn bộ customers

## 🎯 Logic phân loại Customer Type

### Quy tắc phân loại:

1. **So sánh total_vip_spent với price thresholds**
2. **Chọn type có threshold cao nhất mà customer đạt được**
3. **Nếu không đạt threshold nào, chọn type có price thấp nhất (Regular)**

### Ví dụ:

```
Customer Types:
- Regular: 0đ
- Premium: 3,000,000đ  
- VIP: 5,000,000đ

Customer A đã chi: 4,000,000đ
→ Được xếp loại: Premium (đạt threshold 3tr, chưa đạt 5tr)

Customer B đã chi: 6,000,000đ  
→ Được xếp loại: VIP (đạt cả threshold 3tr và 5tr)
```

## 📱 Frontend Updates

### 1. EditCustomer Page

- Hiển thị `Total VIP Spent` trong phần preview
- Format tiền tệ theo chuẩn Việt Nam (VNĐ)

### 2. Navigation Fixes

- ViewCourse, EditCourse, NewCourse: Back button về đúng tab "courses"
- AdminPage: Xử lý navigation state để chuyển đúng tab

## 🧪 Testing

### 1. Test Manual

1. **Tạo VIP payment thành công:**
   - Kiểm tra `total_vip_spent` tăng đúng số tiền
   - Kiểm tra customer type được cập nhật (nếu cần)

2. **Hoàn tiền (refund):**
   - Chuyển payment từ SUCCESS sang FAILED/PENDING
   - Kiểm tra `total_vip_spent` giảm đúng số tiền

3. **Sync endpoints:**
   - Gọi `/sync-customer-vip-spending`
   - Gọi `/auto-classify-customer-types`
   - Kiểm tra dữ liệu customers đã được cập nhật

### 2. Test Customer Type Classification

```bash
# Scenario test:
1. Tạo customer types với price khác nhau
2. Tạo customers với total_vip_spent khác nhau
3. Chạy auto-classify
4. Verify kết quả phân loại đúng
```

## ⚠️ Lưu ý quan trọng

### 1. Data Consistency

- Luôn chạy sync khi có thay đổi lớn về payment data
- Backup database trước khi chạy migration
- Monitor performance khi chạy full sync với số lượng customer lớn

### 2. Performance

- Các query đã được tối ưu với index
- Full sync nên chạy trong giờ thấp điểm
- Có thể chạy async cho số lượng customer lớn (>1000)

### 3. Business Logic

- Chỉ tính payments có status = SUCCESS
- Không tính các giao dịch bị hủy hoặc pending
- Customer type chỉ được nâng cấp, không tự động hạ cấp (trừ khi có refund)

## 🔄 Maintenance

### Daily Tasks

```sql
-- Kiểm tra customers có total_vip_spent không khớp với payment history
SELECT c.customer_id, c.customer_name, c.total_vip_spent,
       (SELECT COALESCE(SUM(amount), 0) FROM vip_payment vp 
        WHERE vp.customer_id = c.customer_id AND payment_status = 'SUCCESS') as actual_total
FROM customer c
WHERE c.total_vip_spent != (SELECT COALESCE(SUM(amount), 0) FROM vip_payment vp 
                            WHERE vp.customer_id = c.customer_id AND payment_status = 'SUCCESS');
```

### Weekly Tasks

1. Chạy full sync để đảm bảo data consistency
2. Review customer type distribution
3. Check for any anomalies in spending patterns

## 📊 Monitoring & Alerts

### Metrics to monitor:

1. **Data consistency:** % customers có total_vip_spent chính xác
2. **Performance:** Thời gian response của sync operations
3. **Business metrics:** Phân bố customer types, average spending

### Alerts:

1. Khi có >5% customers với inconsistent data
2. Khi sync operations mất >30 giây
3. Khi có unusual spending patterns

---

**Cập nhật:** 2024-01-15  
**Version:** 1.0  
**Tác giả:** Development Team 