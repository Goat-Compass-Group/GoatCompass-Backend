package com.goatcompass.taxonomy.application.service;

import com.goatcompass.taxonomy.api.TaxonomyImportApi.CatalogData;
import com.goatcompass.taxonomy.api.TaxonomyImportApi.CredentialData;
import com.goatcompass.taxonomy.api.TaxonomyImportApi.JobFamilyData;
import com.goatcompass.taxonomy.api.TaxonomyImportApi.LeveledSkillData;
import com.goatcompass.taxonomy.api.TaxonomyImportApi.OccupationData;
import com.goatcompass.taxonomy.api.TaxonomyImportApi.SkillData;
import com.goatcompass.taxonomy.domain.model.TaxonomyCatalog;
import com.goatcompass.taxonomy.domain.model.aggregate.Credential;
import com.goatcompass.taxonomy.domain.model.aggregate.JobFamily;
import com.goatcompass.taxonomy.domain.model.aggregate.Occupation;
import com.goatcompass.taxonomy.domain.model.aggregate.Skill;
import com.goatcompass.taxonomy.domain.model.enumeration.Acquisition;
import com.goatcompass.taxonomy.domain.model.enumeration.CredentialKind;
import com.goatcompass.taxonomy.domain.model.enumeration.CredentialRequirement;
import com.goatcompass.taxonomy.domain.model.enumeration.ExternalScheme;
import com.goatcompass.taxonomy.domain.model.enumeration.Importance;
import com.goatcompass.taxonomy.domain.model.enumeration.MatchType;
import com.goatcompass.taxonomy.domain.model.enumeration.RefEntityType;
import com.goatcompass.taxonomy.domain.model.enumeration.RelationType;
import com.goatcompass.taxonomy.domain.model.enumeration.SkillKind;
import com.goatcompass.taxonomy.domain.model.enumeration.Status;
import com.goatcompass.taxonomy.domain.model.valueobject.Alias;
import com.goatcompass.taxonomy.domain.model.valueobject.ExternalReference;
import com.goatcompass.taxonomy.domain.model.valueobject.ProficiencyLevel;
import com.goatcompass.taxonomy.domain.model.valueobject.SkillRelation;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import com.goatcompass.taxonomy.domain.service.AliasNormalizer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class TaxonomyCatalogAssembler {

    private final List<String> errors = new ArrayList<>();

    static Assembled assemble(CatalogData data) {
        TaxonomyCatalogAssembler a = new TaxonomyCatalogAssembler();
        List<JobFamily> families = a.each(data.jobFamilies(), JobFamilyData::code, "Lĩnh vực", a::jobFamily);
        List<Skill> skills = a.each(data.skills(), SkillData::code, "Kỹ năng", a::skill);
        List<Credential> credentials = a.each(data.credentials(), CredentialData::code, "Chứng chỉ", a::credential);
        List<Occupation> occupations = a.each(data.occupations(), OccupationData::code, "Nghề", a::occupation);
        List<ExternalReference> refs = new ArrayList<>();
        for (OccupationData o : data.occupations()) {
            refs.addAll(a.each(list(o.externalRefs()), r -> o.code() + " → " + r.scheme(), "Liên kết chuẩn ngoài",
                    r -> new ExternalReference(RefEntityType.OCCUPATION, TaxonomyCode.of(o.code()),
                            parse(ExternalScheme.class, r.scheme(), "scheme"), r.code(),
                            parse(MatchType.class, r.match(), "match"))));
        }
        return new Assembled(new TaxonomyCatalog(families, skills, credentials, occupations, refs), a.errors);
    }

    private <D, T> List<T> each(List<D> items, Function<D, String> code, String type, Function<D, T> build) {
        List<T> result = new ArrayList<>();
        for (D item : list(items)) {
            try {
                result.add(build.apply(item));
            } catch (IllegalArgumentException | NullPointerException e) {
                errors.add(type + " '" + code.apply(item) + "': " + e.getMessage());
            }
        }
        return result;
    }

    private JobFamily jobFamily(JobFamilyData d) {
        return JobFamily.builder()
                .code(TaxonomyCode.of(d.code()))
                .nameVi(required(d.nameVi(), "name_vi"))
                .nameEn(required(d.nameEn(), "name_en"))
                .descriptionVi(d.descriptionVi())
                .status(d.status() == null ? Status.DRAFT : parse(Status.class, d.status(), "status"))
                .sortOrder(d.sortOrder() == null ? 0 : d.sortOrder())
                .build();
    }

    private Skill skill(SkillData d) {
        List<SkillRelation> relations = new ArrayList<>();
        list(d.broader()).forEach(c -> relations.add(new SkillRelation(RelationType.BROADER, TaxonomyCode.of(c), null)));
        list(d.prerequisites()).forEach(c ->
                relations.add(new SkillRelation(RelationType.PREREQUISITE, TaxonomyCode.of(c), null)));
        list(d.related()).forEach(r -> {
            if (r.weight() == null || r.weight().signum() <= 0 || r.weight().compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("related '" + r.skill() + "': weight phải trong (0, 1]");
            }
            relations.add(new SkillRelation(RelationType.RELATED, TaxonomyCode.of(r.skill()), r.weight()));
        });
        return Skill.builder()
                .code(TaxonomyCode.of(d.code()))
                .nameVi(required(d.nameVi(), "name_vi"))
                .nameEn(required(d.nameEn(), "name_en"))
                .descriptionVi(d.descriptionVi())
                .kind(parse(SkillKind.class, d.kind(), "kind"))
                .acquisition(parse(Acquisition.class, d.acquisition(), "acquisition"))
                .weeksToWorking(d.weeksToWorking())
                .status(d.status() == null ? Status.ACTIVE : parse(Status.class, d.status(), "status"))
                .replacedBy(code(d.replacedBy()))
                .aliases(aliases(d.nameVi(), d.nameEn(), d.aliases()))
                .relations(relations)
                .build();
    }

    private Credential credential(CredentialData d) {
        return Credential.builder()
                .code(TaxonomyCode.of(d.code()))
                .nameVi(required(d.nameVi(), "name_vi"))
                .nameEn(required(d.nameEn(), "name_en"))
                .kind(parse(CredentialKind.class, d.kind(), "kind"))
                .issuer(d.issuer())
                .url(d.url())
                .prepWeeks(d.prepWeeks())
                .validityMonths(d.validityMonths())
                .noteVi(d.noteVi())
                .status(d.status() == null ? Status.ACTIVE : parse(Status.class, d.status(), "status"))
                .replacedBy(code(d.replacedBy()))
                .skills(list(d.skills()).stream()
                        .map(s -> new Credential.EvidencedSkill(TaxonomyCode.of(s.skill()), ProficiencyLevel.of(s.level())))
                        .toList())
                .build();
    }

    private Occupation occupation(OccupationData d) {
        List<Occupation.SkillRequirement> skills = new ArrayList<>();
        if (d.skills() != null) {
            list(d.skills().essential()).forEach(s -> skills.add(requirement(s, Importance.ESSENTIAL)));
            list(d.skills().optional()).forEach(s -> skills.add(requirement(s, Importance.OPTIONAL)));
        }
        return Occupation.builder()
                .code(TaxonomyCode.of(d.code()))
                .nameVi(required(d.nameVi(), "name_vi"))
                .nameEn(required(d.nameEn(), "name_en"))
                .descriptionVi(d.descriptionVi())
                .status(d.status() == null ? Status.ACTIVE : parse(Status.class, d.status(), "status"))
                .replacedBy(code(d.replacedBy()))
                .jobFamilies(list(d.jobFamilies()).stream().map(TaxonomyCode::of).toList())
                .aliases(aliases(null, null, d.aliases()))
                .skills(skills)
                .credentials(list(d.credentials()).stream()
                        .map(c -> new Occupation.CredentialRequirementLink(TaxonomyCode.of(c.credential()),
                                parse(CredentialRequirement.class, c.requirement(), "requirement"), c.noteVi()))
                        .toList())
                .build();
    }

    private static Occupation.SkillRequirement requirement(LeveledSkillData s, Importance importance) {
        return new Occupation.SkillRequirement(TaxonomyCode.of(s.skill()), importance, ProficiencyLevel.of(s.level()));
    }

    /**
     * Alias = tên tiếng Việt + tên tiếng Anh (nếu truyền vào) + danh sách aliases; chuẩn hoá, bỏ trùng, giữ lần đầu.
     * Với nghề, tên hiển thị không tự thành alias (alias nghề dùng để nhận ra tiêu đề JD).
     */
    private static List<Alias> aliases(String nameVi, String nameEn, Map<String, List<String>> extra) {
        Map<String, Alias> byNormalized = new LinkedHashMap<>();
        addAlias(byNormalized, nameVi, "vi");
        addAlias(byNormalized, nameEn, "en");
        if (extra != null) {
            extra.forEach((lang, values) -> {
                if (!lang.equals("vi") && !lang.equals("en")) {
                    throw new IllegalArgumentException("aliases chỉ nhận ngôn ngữ vi, en (gặp '" + lang + "')");
                }
                list(values).forEach(v -> addAlias(byNormalized, v, lang));
            });
        }
        return List.copyOf(byNormalized.values());
    }

    private static void addAlias(Map<String, Alias> target, String text, String lang) {
        if (text != null && !text.isBlank()) {
            String normalized = AliasNormalizer.normalize(text);
            target.putIfAbsent(normalized, new Alias(text.strip(), normalized, lang));
        }
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("thiếu " + field);
        }
        try {
            return Enum.valueOf(type, value.strip());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(field + " '" + value + "' không hợp lệ, chỉ nhận "
                    + Arrays.toString(type.getEnumConstants()));
        }
    }

    private static TaxonomyCode code(String value) {
        return value == null ? null : TaxonomyCode.of(value);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("thiếu " + field);
        }
        return value.strip();
    }

    private static <T> List<T> list(List<T> values) {
        return values == null ? List.of() : values;
    }

    record Assembled(TaxonomyCatalog catalog, List<String> errors) {
    }
}
