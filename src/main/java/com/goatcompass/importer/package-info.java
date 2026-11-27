/**
 * Module Data import: nạp dữ liệu gốc (đặt trong {@code resources/data/}) vào các module nghiệp vụ.
 * Chỉ đọc nguồn và gọi API công khai của module đích (ví dụ {@code TaxonomyImportApi});
 * việc kiểm tra và lưu thuộc về module đích. Chạy lúc khởi động khi bật {@code app.data-import.enabled=true}.
 * <p>
 * Cấu trúc: application / domain / infrastructure (Hexagonal). Xem docs/architecture.md.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Data import")
package com.goatcompass.importer;