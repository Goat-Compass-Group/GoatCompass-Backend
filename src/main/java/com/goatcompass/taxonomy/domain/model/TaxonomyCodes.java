package com.goatcompass.taxonomy.domain.model;

import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import java.util.Set;

public record TaxonomyCodes(
        Set<TaxonomyCode> jobFamilies, Set<TaxonomyCode> skills,
        Set<TaxonomyCode> credentials, Set<TaxonomyCode> occupations
) {

    public static TaxonomyCodes empty() {
        return new TaxonomyCodes(Set.of(), Set.of(), Set.of(), Set.of());
    }
}
