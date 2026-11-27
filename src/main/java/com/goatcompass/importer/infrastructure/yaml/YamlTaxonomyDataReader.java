package com.goatcompass.importer.infrastructure.yaml;

import com.goatcompass.importer.domain.repository.TaxonomyDataReader;
import com.goatcompass.taxonomy.api.TaxonomyImportApi.CatalogData;
import com.goatcompass.taxonomy.api.TaxonomyImportApi.CredentialData;
import com.goatcompass.taxonomy.api.TaxonomyImportApi.JobFamilyData;
import com.goatcompass.taxonomy.api.TaxonomyImportApi.OccupationData;
import com.goatcompass.taxonomy.api.TaxonomyImportApi.SkillData;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.dataformat.yaml.YAMLMapper;

@Component
public class YamlTaxonomyDataReader implements TaxonomyDataReader {

    static final String ROOT = "classpath*:data/taxonomy/";

    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final YAMLMapper mapper = YAMLMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Override
    public CatalogData read() {
        return new CatalogData(
                read("job-families.yaml", JobFamiliesFile.class, JobFamiliesFile::jobFamilies),
                read("skills/*.yaml", SkillsFile.class, SkillsFile::skills),
                read("credentials.yaml", CredentialsFile.class, CredentialsFile::credentials),
                read("occupations/*.yaml", OccupationsFile.class, OccupationsFile::occupations));
    }

    private <F, T> List<T> read(String pattern, Class<F> fileType, Function<F, List<T>> items) {
        List<T> result = new ArrayList<>();
        for (Resource resource : resources(pattern)) {
            try (InputStream in = resource.getInputStream()) {
                F file = mapper.readValue(in, fileType);
                if (file != null && items.apply(file) != null) {
                    result.addAll(items.apply(file));
                }
            } catch (IOException | JacksonException e) {
                throw new IllegalArgumentException("File " + resource.getFilename() + ": YAML không hợp lệ: "
                        + e.getMessage(), e);
            }
        }
        return result;
    }

    private List<Resource> resources(String pattern) {
        try {
            Resource[] found = resolver.getResources(ROOT + pattern);
            if (found.length == 0) {
                throw new IllegalArgumentException("Không tìm thấy dữ liệu taxonomy: " + ROOT + pattern);
            }
            return Arrays.stream(found).sorted(Comparator.comparing(Resource::getFilename)).toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("Không đọc được " + ROOT + pattern + ": " + e.getMessage(), e);
        }
    }

    /** Cấu trúc gốc của từng loại file YAML. */
    record JobFamiliesFile(List<JobFamilyData> jobFamilies) {
    }

    record SkillsFile(List<SkillData> skills) {
    }

    record CredentialsFile(List<CredentialData> credentials) {
    }

    record OccupationsFile(List<OccupationData> occupations) {
    }
}
