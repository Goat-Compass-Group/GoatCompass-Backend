package com.goatcompass.taxonomy.domain.model.aggregate;

import com.goatcompass.taxonomy.domain.model.valueobject.Alias;
import com.goatcompass.taxonomy.domain.model.valueobject.ProficiencyLevel;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;

import java.util.List;
import java.util.Objects;

import com.goatcompass.taxonomy.domain.model.enumeration.CredentialRequirement;
import com.goatcompass.taxonomy.domain.model.enumeration.Importance;
import com.goatcompass.taxonomy.domain.model.enumeration.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

/**
 * Aggregate root: nghề. Không gắn cấp bậc; junior/senior thể hiện qua mức độ kỹ năng trong JD.
 * Hồ sơ kỹ năng ({@link #skills}) là mặc định khi người dùng chọn nghề thay vì dán JD.
 */
@Getter
public class Occupation {
    private final TaxonomyCode code;
    private final String nameVi;
    private final String nameEn;
    private final String descriptionVi;
    private final Status status;
    private final TaxonomyCode replacedBy;
    private final List<TaxonomyCode> jobFamilies;
    private final List<Alias> aliases;
    private final List<SkillRequirement> skills;
    private final List<CredentialRequirementLink> credentials;

    @Builder
    private Occupation(
            TaxonomyCode code, String nameVi, String nameEn, String descriptionVi, Status status,
            TaxonomyCode replacedBy, @Singular List<TaxonomyCode> jobFamilies, @Singular List<Alias> aliases,
            @Singular List<SkillRequirement> skills, @Singular List<CredentialRequirementLink> credentials
    ) {
        this.code = Objects.requireNonNull(code, "code");
        this.nameVi = Objects.requireNonNull(nameVi, "nameVi");
        this.nameEn = Objects.requireNonNull(nameEn, "nameEn");
        this.descriptionVi = descriptionVi;
        this.status = status == null ? Status.ACTIVE : status;
        this.replacedBy = replacedBy;
        this.jobFamilies = List.copyOf(jobFamilies);
        this.aliases = List.copyOf(aliases);
        this.skills = List.copyOf(skills);
        this.credentials = List.copyOf(credentials);

        if (this.jobFamilies.isEmpty()) {
            throw new IllegalArgumentException("Nghề phải thuộc ít nhất một lĩnh vực: " + code);
        }
    }

    public TaxonomyCode primaryJobFamily() {
        return jobFamilies.getFirst();
    }

    /** Nghề có hoạt động cần giấy phép/chứng chỉ bắt buộc theo luật: lộ trình phải nói rõ "không có đường tắt". */
    public boolean isRegulated() {
        return credentials.stream().anyMatch(c -> c.requirement() == CredentialRequirement.LEGALLY_REQUIRED);
    }

    public record SkillRequirement(TaxonomyCode skill, Importance importance, ProficiencyLevel level) {
    }

    public record CredentialRequirementLink(TaxonomyCode credential, CredentialRequirement requirement, String noteVi) {
    }
}