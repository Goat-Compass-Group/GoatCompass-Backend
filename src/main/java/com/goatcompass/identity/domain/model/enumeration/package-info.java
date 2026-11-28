/**
 * Enum nghiệp vụ của module identity:
 * <ul>
 *   <li>{@link com.goatcompass.identity.domain.model.enumeration.UserStatus}: INACTIVE (chờ OTP), ACTIVE, BLOCKED.</li>
 *   <li>{@link com.goatcompass.identity.domain.model.enumeration.Role}: USER, ADMIN.</li>
 * </ul>
 * Lưu xuống DB dạng chuỗi và có CHECK constraint trong changelog của bảng {@code users}.
 * <b>Thêm giá trị mới thì phải thêm changeset sửa CHECK tương ứng.</b>
 */
package com.goatcompass.identity.domain.model.enumeration;
