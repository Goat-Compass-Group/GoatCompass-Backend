package com.goatcompass.importer.domain.repository;

import com.goatcompass.taxonomy.api.TaxonomyImportApi;

public interface TaxonomyDataReader {
    TaxonomyImportApi.CatalogData read();
}
