/**
 * Mô hình nghiệp vụ của taxonomy (Java thuần, không phụ thuộc Spring/JPA), chia theo vai trò DDD:
 * <ul>
 *   <li>{@code aggregate}: aggregate root có định danh và vòng đời, được tải/lưu nguyên khối qua repository.</li>
 *   <li>{@code valueobject}: giá trị bất biến, không có định danh, tự kiểm tra tính hợp lệ.</li>
 *   <li>{@code enumeration}: enum nghiệp vụ. Đây là phần DUY NHẤT của domain mà JPA entity dùng trực tiếp
 *       ({@code @Enumerated}); aggregate và value object chỉ gặp entity qua mapper ở infrastructure.</li>
 * </ul>
 */
package com.goatcompass.taxonomy.domain.model;
