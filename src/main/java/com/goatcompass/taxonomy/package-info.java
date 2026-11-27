/**
 * Module Taxonomy: từ điển dùng chung về lĩnh vực, nghề, kỹ năng và chứng chỉ (docs/taxonomy.md).
 * Dữ liệu gốc: resources/data/taxonomy/*.yaml, nạp qua TaxonomyImportApi (module dataimport).
 * <p>
 * Module khác chỉ được dùng {@link com.goatcompass.taxonomy.api.TaxonomyApi} và tham chiếu bằng mã ({@code code}).
 * Cấu trúc: api / application / domain / infrastructure (Hexagonal + DDD). Xem docs/architecture.md.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Taxonomy")
package com.goatcompass.taxonomy;
