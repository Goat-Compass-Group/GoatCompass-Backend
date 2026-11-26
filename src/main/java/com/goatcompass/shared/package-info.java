/**
 * Module dùng chung (tương đương shared-kernel + cấu hình chung của GoatSports): cấu hình JPA, Liquibase, Security,
 * filter, xử lý lỗi, định dạng response. Module OPEN: mọi module khác được dùng trực tiếp.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Shared", type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.goatcompass.shared;
