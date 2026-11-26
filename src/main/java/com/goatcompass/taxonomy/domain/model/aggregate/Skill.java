package com.goatcompass.taxonomy.domain.model.aggregate;

import com.goatcompass.taxonomy.domain.model.valueobject.Alias;
import com.goatcompass.taxonomy.domain.model.valueobject.SkillRelation;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;

import java.util.List;
import java.util.Objects;

import com.goatcompass.taxonomy.domain.model.enumeration.Acquisition;
import com.goatcompass.taxonomy.domain.model.enumeration.RelationType;
import com.goatcompass.taxonomy.domain.model.enumeration.SkillKind;
import com.goatcompass.taxonomy.domain.model.enumeration.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

/**
 * Aggregate root: kỹ năng, kiến thức, công cụ, ngoại ngữ hoặc kỹ năng mềm. Dùng chung mọi lĩnh vực.
 */
@Getter
public class Skill {
    /** Lộ trình MVP dài 4 tuần: chỉ kỹ năng QUICK (≤ 4 tuần tới mức 2) mới được đưa trọn vào lộ trình. */
    public static final int ROADMAP_WEEKS = 4;

    private final TaxonomyCode code;
    private final String nameVi;
    private final String nameEn;
    private final String descriptionVi;
    private final SkillKind kind;
    private final Acquisition acquisition;
    private final Integer weeksToWorking;
    private Status status;
    private TaxonomyCode replacedBy;
    private final List<Alias> aliases;
    private final List<SkillRelation> relations;

    @Builder
    private Skill(
            TaxonomyCode code, String nameVi, String nameEn, String descriptionVi, SkillKind kind,
            Acquisition acquisition, Integer weeksToWorking, Status status, TaxonomyCode replacedBy,
            @Singular List<Alias> aliases, @Singular List<SkillRelation> relations
    ) {
        this.code = Objects.requireNonNull(code, "code");
        this.nameVi = Objects.requireNonNull(nameVi, "nameVi");
        this.nameEn = Objects.requireNonNull(nameEn, "nameEn");
        this.descriptionVi = descriptionVi;
        this.kind = Objects.requireNonNull(kind, "kind");
        this.acquisition = Objects.requireNonNull(acquisition, "acquisition");
        this.weeksToWorking = weeksToWorking;
        this.status = status == null ? Status.ACTIVE : status;
        this.replacedBy = replacedBy;
        this.aliases = List.copyOf(aliases);
        this.relations = List.copyOf(relations);

        if (acquisition == Acquisition.QUICK && (weeksToWorking == null || weeksToWorking > ROADMAP_WEEKS)) {
            throw new IllegalArgumentException("Kỹ năng QUICK phải có weeksToWorking ≤ " + ROADMAP_WEEKS + ": " + code);
        }
    }

    /** Có thể đạt mức "làm được việc" trong lộ trình 4 tuần hay không. */
    public boolean fitsRoadmap() {
        return acquisition == Acquisition.QUICK;
    }

    public List<TaxonomyCode> related(RelationType type) {
        return relations.stream().filter(r -> r.type() == type).map(SkillRelation::target).toList();
    }

    /** Mã không bao giờ bị xoá: lỗi thời thì đánh dấu DEPRECATED và trỏ sang kỹ năng thay thế. */
    public void deprecate(TaxonomyCode replacement) {
        if (code.equals(replacement)) {
            throw new IllegalArgumentException("Kỹ năng không thể thay thế bằng chính nó: " + code);
        }

        this.status = Status.DEPRECATED;
        this.replacedBy = replacement;
    }
}