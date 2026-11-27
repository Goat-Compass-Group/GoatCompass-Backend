package com.goatcompass.taxonomy.application.service;

import com.goatcompass.taxonomy.api.TaxonomyImportApi;
import com.goatcompass.taxonomy.domain.model.TaxonomyWriteSummary;
import com.goatcompass.taxonomy.domain.repository.TaxonomyCatalogWriter;
import com.goatcompass.taxonomy.domain.service.TaxonomyCatalogValidator;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaxonomyImportService implements TaxonomyImportApi {

    private final TaxonomyCatalogWriter writer;

    @Override
    @Transactional
    public ImportResult importCatalog(CatalogData data, boolean dryRun) {
        TaxonomyCatalogAssembler.Assembled assembled = TaxonomyCatalogAssembler.assemble(data);
        TaxonomyCatalogValidator.Result validation =
                TaxonomyCatalogValidator.validate(assembled.catalog(), writer.existingCodes());

        List<String> errors = new ArrayList<>(assembled.errors());
        errors.addAll(validation.errors());
        if (!errors.isEmpty() || dryRun) {
            return new ImportResult(dryRun, errors, validation.warnings(), List.of());
        }
        TaxonomyWriteSummary summary = writer.write(assembled.catalog());
        return new ImportResult(false, List.of(), validation.warnings(), summary.lines().stream()
                .map(l -> new Count(l.type(), l.created(), l.updated(), l.unchanged()))
                .toList());
    }
}
