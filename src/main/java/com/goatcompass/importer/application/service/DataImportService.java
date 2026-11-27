package com.goatcompass.importer.application.service;

import com.goatcompass.importer.application.exception.DataImportException;
import com.goatcompass.importer.domain.repository.TaxonomyDataReader;
import com.goatcompass.taxonomy.api.TaxonomyImportApi;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataImportService {
    private final TaxonomyDataReader taxonomyData;
    private final TaxonomyImportApi taxonomy;

    public TaxonomyImportApi.ImportResult importTaxonomy(boolean dryRun) throws DataImportException {
        TaxonomyImportApi.CatalogData data;
        try {
            data = taxonomyData.read();
        } catch (IllegalArgumentException e) {
            throw new DataImportException("taxonomy", List.of(e.getMessage()));
        }
        TaxonomyImportApi.ImportResult result = taxonomy.importCatalog(data, dryRun);
        if (!result.isValid()) {
            throw new DataImportException("taxonomy", result.errors());
        }
        return result;
    }
}
