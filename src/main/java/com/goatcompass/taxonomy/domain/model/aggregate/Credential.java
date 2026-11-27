package com.goatcompass.taxonomy.domain.model.aggregate;

import com.goatcompass.taxonomy.domain.model.valueobject.ProficiencyLevel;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;

import java.util.List;

import com.goatcompass.taxonomy.domain.model.enumeration.CredentialKind;
import com.goatcompass.taxonomy.domain.model.enumeration.Status;
import lombok.Builder;
import lombok.Singular;

/**
 * @param prepWeeks      ước tính thời gian chuẩn bị/đào tạo (tuần)
 * @param validityMonths null = không hết hạn hoặc chưa xác minh
 * @param skills         có chứng chỉ thì được xem là đạt các kỹ năng này ở mức tương ứng
 */
@Builder
public record Credential(
        TaxonomyCode code, String nameVi, String nameEn, CredentialKind kind, String issuer,
        String url, Integer prepWeeks, Integer validityMonths, String noteVi, Status status, TaxonomyCode replacedBy,
        @Singular List<EvidencedSkill> skills
) {

    public Credential {
        skills = List.copyOf(skills);
    }

    /** Lấy được trong lộ trình 4 tuần không. Giấy phép và bằng cấp thì không bao giờ. */
    public boolean fitsRoadmap() {
        return kind == CredentialKind.CERTIFICATE && prepWeeks != null && prepWeeks <= Skill.ROADMAP_WEEKS;
    }

    public record EvidencedSkill(TaxonomyCode skill, ProficiencyLevel level) {
    }
}
