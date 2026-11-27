package com.goatcompass.taxonomy.domain.repository;

import com.goatcompass.taxonomy.domain.model.TaxonomyCatalog;
import com.goatcompass.taxonomy.domain.model.TaxonomyCodes;
import com.goatcompass.taxonomy.domain.model.TaxonomyWriteSummary;

public interface TaxonomyCatalogWriter {
    TaxonomyCodes existingCodes();

    TaxonomyWriteSummary write(TaxonomyCatalog catalog);
}
