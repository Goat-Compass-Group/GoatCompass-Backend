package com.goatcompass.taxonomy.domain.model.aggregate;

import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;

import com.goatcompass.taxonomy.domain.model.enumeration.Status;
import lombok.Builder;

/** Lĩnh vực nghề nghiệp ("ngành" trong tài liệu sản phẩm): marketing, accounting-finance... */
@Builder
public record JobFamily(
        TaxonomyCode code, String nameVi, String nameEn, String descriptionVi,
        Status status, int sortOrder
) {
    public boolean isVisible() {
        return status == Status.ACTIVE;
    }
}
