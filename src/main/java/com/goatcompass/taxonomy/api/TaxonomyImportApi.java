package com.goatcompass.taxonomy.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TaxonomyImportApi {
    ImportResult importCatalog(CatalogData catalog, boolean dryRun);

    record CatalogData(
            List<JobFamilyData> jobFamilies, List<SkillData> skills,
            List<CredentialData> credentials, List<OccupationData> occupations
    ) {
        public CatalogData {
            jobFamilies = jobFamilies == null ? List.of() : List.copyOf(jobFamilies);
            skills = skills == null ? List.of() : List.copyOf(skills);
            credentials = credentials == null ? List.of() : List.copyOf(credentials);
            occupations = occupations == null ? List.of() : List.copyOf(occupations);
        }
    }

    record JobFamilyData(
            String code, String nameVi, String nameEn, String descriptionVi,
            String status, Integer sortOrder
    ) {
    }

    record SkillData(
            String code, String nameVi, String nameEn, String descriptionVi, String kind,
            String acquisition, Integer weeksToWorking, List<String> broader, List<RelatedData> related,
            List<String> prerequisites, Map<String, List<String>> aliases, String status, String replacedBy
    ) {
    }

    record RelatedData(String skill, BigDecimal weight) {
    }

    record CredentialData(
            String code, String nameVi, String nameEn, String kind, String issuer, String url,
            Integer prepWeeks, Integer validityMonths, List<LeveledSkillData> skills, String noteVi,
            String status, String replacedBy
    ) {
    }

    record LeveledSkillData(String skill, int level) {
    }

    record OccupationData(
            String code, String nameVi, String nameEn, List<String> jobFamilies, String descriptionVi,
            Map<String, List<String>> aliases, List<ExternalRefData> externalRefs,
            OccupationSkillsData skills, List<OccupationCredentialData> credentials,
            String status, String replacedBy
    ) {
    }

    record ExternalRefData(String scheme, String code, String match) {
    }

    record OccupationSkillsData(List<LeveledSkillData> essential, List<LeveledSkillData> optional) {
    }

    record OccupationCredentialData(String credential, String requirement, String noteVi) {
    }

    record ImportResult(boolean dryRun, List<String> errors, List<String> warnings, List<Count> counts) {

        public ImportResult {
            errors = List.copyOf(errors);
            warnings = List.copyOf(warnings);
            counts = List.copyOf(counts);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }
    }

    record Count(String type, int created, int updated, int unchanged) {
    }
}
