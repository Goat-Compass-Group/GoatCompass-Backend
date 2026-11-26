package com.goatcompass.taxonomy.infrastructure.web.dto;

import com.goatcompass.taxonomy.api.TaxonomyApi.OccupationProfile;
import java.util.List;

public record OccupationResponse(
        String code, String nameVi, String nameEn, String primaryJobFamily, boolean regulated,
        List<Skill> skills, List<Credential> credentials
) {

    public record Skill(String code, boolean essential, int level) {
    }

    public record Credential(String code, String requirement, String noteVi) {
    }

    public static OccupationResponse from(OccupationProfile o) {
        return new OccupationResponse(
            o.code(), o.nameVi(), o.nameEn(), o.primaryJobFamily(), o.regulated(),
            o.skills().stream().map(s -> new Skill(s.skillCode(), s.essential(), s.level())).toList(),
            o.credentials().stream().map(c -> new Credential(c.credentialCode(), c.requirement(), c.noteVi())).toList()
        );
    }
}