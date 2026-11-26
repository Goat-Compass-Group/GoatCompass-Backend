package com.goatcompass.taxonomy.infrastructure.web.dto;

import com.goatcompass.taxonomy.api.TaxonomyApi.SkillSummary;

public record SkillResponse(
        String code, String nameVi, String nameEn, String kind, String acquisition,
        Integer weeksToWorking, boolean fitsRoadmap, boolean deprecated, String replacedBy
) {
    public static SkillResponse from(SkillSummary s) {
        return new SkillResponse(
                s.code(), s.nameVi(), s.nameEn(), s.kind(), s.acquisition(),
                s.weeksToWorking(), s.fitsRoadmap(), s.deprecated(), s.replacedBy()
        );
    }
}