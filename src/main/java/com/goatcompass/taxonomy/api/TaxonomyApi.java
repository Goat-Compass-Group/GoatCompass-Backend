package com.goatcompass.taxonomy.api;

import java.util.List;
import java.util.Optional;

public interface TaxonomyApi {
    Optional<SkillSummary> findSkill(String code);

    List<SkillSummary> resolveSkills(String text);

    Optional<OccupationProfile> findOccupation(String code);

    List<JobFamilySummary> listJobFamilies();

    record SkillSummary(
            String code, String nameVi, String nameEn, String kind, String acquisition,
            Integer weeksToWorking, boolean fitsRoadmap, boolean deprecated, String replacedBy
    ) {
    }

    record OccupationProfile(
            String code, String nameVi, String nameEn, String primaryJobFamily,
            boolean regulated, List<RequiredSkill> skills, List<RequiredCredential> credentials
    ) {

        public record RequiredSkill(String skillCode, boolean essential, int level) {
        }

        public record RequiredCredential(String credentialCode, String requirement, String noteVi) {
        }
    }

    record JobFamilySummary(String code, String nameVi, String nameEn, boolean visible) {
    }
}
