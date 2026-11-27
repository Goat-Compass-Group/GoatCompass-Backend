package com.goatcompass.taxonomy.domain.model.valueobject;

import com.goatcompass.taxonomy.domain.model.enumeration.ExternalScheme;
import com.goatcompass.taxonomy.domain.model.enumeration.MatchType;
import com.goatcompass.taxonomy.domain.model.enumeration.RefEntityType;
import java.util.Objects;

public record ExternalReference(
        RefEntityType entityType, TaxonomyCode entityCode,
        ExternalScheme scheme, String externalCode, MatchType matchType
) {
    public ExternalReference {
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(entityCode, "entityCode");
        Objects.requireNonNull(scheme, "scheme");
        Objects.requireNonNull(matchType, "matchType");

        if (externalCode == null || externalCode.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã bên ngoài cho " + entityCode + " (" + scheme + ")");
        }
    }
}
