package com.goatcompass.importer;

import static org.assertj.core.api.Assertions.assertThat;

import com.goatcompass.TestcontainersConfiguration;
import com.goatcompass.importer.application.service.DataImportService;
import com.goatcompass.taxonomy.api.TaxonomyApi;
import com.goatcompass.taxonomy.api.TaxonomyImportApi;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Nạp ĐÚNG bộ dữ liệu thật trong resources/data/taxonomy vào Postgres (Testcontainers).
 * Ai sửa YAML sai thì test này fail. Chạy trong transaction và rollback để không ảnh hưởng test khác.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataImportIntegrationTest {

    @Autowired
    DataImportService dataImport;

    @Autowired
    TaxonomyApi taxonomy;

    @Test
    void importsRealTaxonomyDataAndIsIdempotent() throws Exception {
        TaxonomyImportApi.ImportResult first = dataImport.importTaxonomy(false);
        assertThat(first.errors()).isEmpty();
        Map<String, TaxonomyImportApi.Count> counts = byType(first);
        assertThat(counts.get("job_family").created()).isEqualTo(2);
        assertThat(counts.get("skill").created()).isEqualTo(53);
        assertThat(counts.get("credential").created()).isEqualTo(10);
        assertThat(counts.get("occupation").created()).isEqualTo(9);
        assertThat(counts.get("external_ref").created()).isPositive();

        // Nạp lần hai với cùng dữ liệu: không tạo, không sửa gì
        TaxonomyImportApi.ImportResult second = dataImport.importTaxonomy(false);
        assertThat(second.counts()).allSatisfy(c -> {
            assertThat(c.created()).as(c.type() + " created").isZero();
            assertThat(c.updated()).as(c.type() + " updated").isZero();
        });
    }

    @Test
    void importedDataIsUsableThroughTaxonomyApi() throws Exception {
        dataImport.importTaxonomy(false);

        assertThat(taxonomy.resolveSkills("Chạy ADS  FB")).extracting(TaxonomyApi.SkillSummary::code)
                .containsExactly("meta-ads");
        assertThat(taxonomy.findOccupation("statutory-auditor")).hasValueSatisfying(o -> {
            assertThat(o.regulated()).isTrue();
            assertThat(o.primaryJobFamily()).isEqualTo("accounting-finance");
        });
        assertThat(taxonomy.listJobFamilies()).extracting(TaxonomyApi.JobFamilySummary::code)
                .containsExactly("marketing", "accounting-finance");
    }

    @Test
    void dryRunValidatesWithoutWriting() throws Exception {
        TaxonomyImportApi.ImportResult result = dataImport.importTaxonomy(true);
        assertThat(result.isValid()).isTrue();
        assertThat(result.counts()).isEmpty();
        assertThat(taxonomy.findSkill("meta-ads")).isEmpty();
    }

    private static Map<String, TaxonomyImportApi.Count> byType(TaxonomyImportApi.ImportResult result) {
        return result.counts().stream().collect(Collectors.toMap(TaxonomyImportApi.Count::type, c -> c));
    }
}
