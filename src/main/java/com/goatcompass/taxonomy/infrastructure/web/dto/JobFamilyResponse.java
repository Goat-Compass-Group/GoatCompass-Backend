package com.goatcompass.taxonomy.infrastructure.web.dto;

import com.goatcompass.taxonomy.api.TaxonomyApi.JobFamilySummary;

public record JobFamilyResponse(String code, String nameVi, String nameEn) {

    public static JobFamilyResponse from(JobFamilySummary f) {
        return new JobFamilyResponse(f.code(), f.nameVi(), f.nameEn());
    }

}