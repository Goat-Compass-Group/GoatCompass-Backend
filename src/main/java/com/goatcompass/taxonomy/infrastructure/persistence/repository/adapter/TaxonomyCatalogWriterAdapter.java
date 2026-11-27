package com.goatcompass.taxonomy.infrastructure.persistence.repository.adapter;

import com.goatcompass.taxonomy.domain.model.TaxonomyCatalog;
import com.goatcompass.taxonomy.domain.model.TaxonomyCodes;
import com.goatcompass.taxonomy.domain.model.TaxonomyWriteSummary;
import com.goatcompass.taxonomy.domain.model.aggregate.Credential;
import com.goatcompass.taxonomy.domain.model.aggregate.JobFamily;
import com.goatcompass.taxonomy.domain.model.aggregate.Occupation;
import com.goatcompass.taxonomy.domain.model.aggregate.Skill;
import com.goatcompass.taxonomy.domain.model.enumeration.MatchType;
import com.goatcompass.taxonomy.domain.model.enumeration.RefEntityType;
import com.goatcompass.taxonomy.domain.model.enumeration.RelationType;
import com.goatcompass.taxonomy.domain.model.valueobject.Alias;
import com.goatcompass.taxonomy.domain.model.valueobject.ExternalReference;
import com.goatcompass.taxonomy.domain.model.valueobject.SkillRelation;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import com.goatcompass.taxonomy.domain.repository.TaxonomyCatalogWriter;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.CodedEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.CredentialEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.ExternalRefEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.JobFamilyEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.OccupationCredentialEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.OccupationEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.OccupationJobFamilyEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.OccupationSkillEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.SkillEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.SkillRelationEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.embeddable.AliasEmbeddable;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.embeddable.CredentialSkillEmbeddable;
import com.goatcompass.taxonomy.infrastructure.persistence.repository.CredentialJpaRepository;
import com.goatcompass.taxonomy.infrastructure.persistence.repository.ExternalRefJpaRepository;
import com.goatcompass.taxonomy.infrastructure.persistence.repository.JobFamilyJpaRepository;
import com.goatcompass.taxonomy.infrastructure.persistence.repository.OccupationJpaRepository;
import com.goatcompass.taxonomy.infrastructure.persistence.repository.SkillJpaRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class TaxonomyCatalogWriterAdapter implements TaxonomyCatalogWriter {

    private final JobFamilyJpaRepository jobFamilies;
    private final SkillJpaRepository skills;
    private final CredentialJpaRepository credentials;
    private final OccupationJpaRepository occupations;
    private final ExternalRefJpaRepository externalRefs;

    @Override
    public TaxonomyCodes existingCodes() {
        return new TaxonomyCodes(codes(jobFamilies.findAll()), codes(skills.findAll()),
                codes(credentials.findAll()), codes(occupations.findAll()));
    }

    @Override
    public TaxonomyWriteSummary write(TaxonomyCatalog catalog) {
        Map<String, JobFamilyEntity> familyByCode = byCode(jobFamilies.findAll());
        Map<String, SkillEntity> skillByCode = byCode(skills.findAll());
        Map<String, CredentialEntity> credentialByCode = byCode(credentials.findAll());
        Map<String, OccupationEntity> occupationByCode = byCode(occupations.findAll());

        Counter familyCount = new Counter("job_family");
        for (JobFamily f : catalog.jobFamilies()) {
            JobFamilyEntity e = familyByCode.get(f.code().value());
            boolean created = e == null;
            if (created) {
                e = new JobFamilyEntity(f.code().value(), f.nameVi(), f.nameEn());
            }
            boolean changed = writeJobFamily(e, f);
            if (created) {
                e = jobFamilies.save(e);
                familyByCode.put(f.code().value(), e);
            }
            familyCount.record(created, changed);
        }

        // Kỹ năng: lượt 1 tạo/cập nhật từng kỹ năng; lượt 2 mới nối quan hệ (cần đủ mọi kỹ năng có id trước)
        Map<String, Boolean> skillChanged = new LinkedHashMap<>();
        Map<String, Boolean> skillCreated = new HashMap<>();
        for (Skill s : catalog.skills()) {
            SkillEntity e = skillByCode.get(s.getCode().value());
            boolean created = e == null;
            if (created) {
                e = new SkillEntity(s.getCode().value(), s.getNameVi(), s.getNameEn(), s.getKind(), s.getAcquisition());
            }
            skillChanged.put(s.getCode().value(), writeSkill(e, s));
            if (created) {
                e = skills.save(e);
                skillByCode.put(s.getCode().value(), e);
            }
            skillCreated.put(s.getCode().value(), created);
        }
        Map<String, Map<RelationKey, BigDecimal>> desiredRelations = desiredRelations(catalog.skills());
        for (Skill s : catalog.skills()) {
            SkillEntity e = skillByCode.get(s.getCode().value());
            boolean changed = e.getStatus() != s.getStatus() || !Objects.equals(code(e.getReplacedBy()), s.getReplacedBy());
            if (changed) {
                e.updateStatus(s.getStatus(), s.getReplacedBy() == null ? null : skillByCode.get(s.getReplacedBy().value()));
            }
            changed |= syncRelations(e, desiredRelations.getOrDefault(s.getCode().value(), Map.of()), skillByCode);
            skillChanged.merge(s.getCode().value(), changed, Boolean::logicalOr);
        }
        Counter skillCount = new Counter("skill");
        skillChanged.forEach((code, changed) -> skillCount.record(skillCreated.get(code), changed));

        Counter credentialCount = new Counter("credential");
        for (Credential c : catalog.credentials()) {
            CredentialEntity e = credentialByCode.get(c.code().value());
            boolean created = e == null;
            if (created) {
                e = new CredentialEntity(c.code().value(), c.nameVi(), c.nameEn(), c.kind());
            }
            boolean changed = writeCredential(e, c, skillByCode, credentialByCode);
            if (created) {
                e = credentials.save(e);
                credentialByCode.put(c.code().value(), e);
            }
            credentialCount.record(created, changed);
        }

        Counter occupationCount = new Counter("occupation");
        for (Occupation o : catalog.occupations()) {
            OccupationEntity e = occupationByCode.get(o.getCode().value());
            boolean created = e == null;
            if (created) {
                e = occupations.save(new OccupationEntity(o.getCode().value(), o.getNameVi(), o.getNameEn()));
                occupationByCode.put(o.getCode().value(), e);
            }
            occupationCount.record(created,
                    writeOccupation(e, o, familyByCode, skillByCode, credentialByCode, occupationByCode));
        }

        Counter refCount = syncExternalRefs(catalog, occupationByCode);
        return new TaxonomyWriteSummary(List.of(familyCount.line(), skillCount.line(), credentialCount.line(),
                occupationCount.line(), refCount.line()));
    }

    private static boolean writeJobFamily(JobFamilyEntity e, JobFamily f) {
        boolean changed = setIfDifferent(e.getNameVi(), f.nameVi(), e::setNameVi);
        changed |= setIfDifferent(e.getNameEn(), f.nameEn(), e::setNameEn);
        changed |= setIfDifferent(e.getDescriptionVi(), f.descriptionVi(), e::setDescriptionVi);
        changed |= setIfDifferent(e.getStatus(), f.status(), e::setStatus);
        changed |= setIfDifferent(e.getSortOrder(), f.sortOrder(), e::setSortOrder);
        return changed;
    }

    private static boolean writeSkill(SkillEntity e, Skill s) {
        boolean changed = setIfDifferent(e.getNameVi(), s.getNameVi(), e::setNameVi);
        changed |= setIfDifferent(e.getNameEn(), s.getNameEn(), e::setNameEn);
        changed |= setIfDifferent(e.getDescriptionVi(), s.getDescriptionVi(), e::setDescriptionVi);
        changed |= setIfDifferent(e.getKind(), s.getKind(), e::setKind);
        changed |= setIfDifferent(e.getAcquisition(), s.getAcquisition(), e::setAcquisition);
        changed |= setIfDifferent(e.getWeeksToWorking(), toShort(s.getWeeksToWorking()), e::setWeeksToWorking);
        changed |= syncAliases(e.getAliases(), s.getAliases());
        return changed;
    }

    private static boolean writeCredential(
            CredentialEntity e, Credential c, Map<String, SkillEntity> skillByCode,
            Map<String, CredentialEntity> credentialByCode
    ) {
        boolean changed = setIfDifferent(e.getNameVi(), c.nameVi(), e::setNameVi);
        changed |= setIfDifferent(e.getNameEn(), c.nameEn(), e::setNameEn);
        changed |= setIfDifferent(e.getKind(), c.kind(), e::setKind);
        changed |= setIfDifferent(e.getIssuer(), c.issuer(), e::setIssuer);
        changed |= setIfDifferent(e.getUrl(), c.url(), e::setUrl);
        changed |= setIfDifferent(e.getPrepWeeks(), toShort(c.prepWeeks()), e::setPrepWeeks);
        changed |= setIfDifferent(e.getValidityMonths(), toShort(c.validityMonths()), e::setValidityMonths);
        changed |= setIfDifferent(e.getNoteVi(), c.noteVi(), e::setNoteVi);
        if (e.getStatus() != c.status() || !Objects.equals(code(e.getReplacedBy()), c.replacedBy())) {
            e.updateStatus(c.status(), c.replacedBy() == null ? null : credentialByCode.get(c.replacedBy().value()));
            changed = true;
        }
        List<String> current = e.getSkills().stream().map(s -> s.getSkill().getCode() + ":" + s.getLevel()).toList();
        List<String> desired = c.skills().stream().map(s -> s.skill().value() + ":" + s.level().value()).toList();
        if (!current.equals(desired)) {
            e.getSkills().clear();
            c.skills().forEach(s -> e.getSkills().add(
                    new CredentialSkillEmbeddable((short) s.level().value(), skillByCode.get(s.skill().value()))));
            changed = true;
        }
        return changed;
    }

    private boolean writeOccupation(
            OccupationEntity e, Occupation o, Map<String, JobFamilyEntity> familyByCode,
            Map<String, SkillEntity> skillByCode,
            Map<String, CredentialEntity> credentialByCode,
            Map<String, OccupationEntity> occupationByCode
    ) {
        boolean changed = setIfDifferent(e.getNameVi(), o.getNameVi(), e::setNameVi);
        changed |= setIfDifferent(e.getNameEn(), o.getNameEn(), e::setNameEn);
        changed |= setIfDifferent(e.getDescriptionVi(), o.getDescriptionVi(), e::setDescriptionVi);
        changed |= syncAliases(e.getAliases(), o.getAliases());
        if (e.getStatus() != o.getStatus() || !Objects.equals(code(e.getReplacedBy()), o.getReplacedBy())) {
            e.updateStatus(o.getStatus(), o.getReplacedBy() == null ? null : occupationByCode.get(o.getReplacedBy().value()));
            changed = true;
        }
        changed |= syncJobFamilies(e, o, familyByCode);

        Map<String, Occupation.SkillRequirement> desiredSkills = o.getSkills().stream()
                .collect(Collectors.toMap(r -> r.skill().value(), r -> r, (a, b) -> a, LinkedHashMap::new));
        changed |= e.getSkills().removeIf(s -> !desiredSkills.containsKey(s.getSkill().getCode()));
        Map<String, OccupationSkillEntity> currentSkills = e.getSkills().stream()
                .collect(Collectors.toMap(s -> s.getSkill().getCode(), s -> s));
        for (Occupation.SkillRequirement r : desiredSkills.values()) {
            OccupationSkillEntity current = currentSkills.get(r.skill().value());
            short level = (short) r.level().value();
            if (current == null) {
                e.getSkills().add(new OccupationSkillEntity(e, skillByCode.get(r.skill().value()), r.importance(), level));
                changed = true;
            } else {
                changed |= setIfDifferent(current.getImportance(), r.importance(), current::setImportance);
                changed |= setIfDifferent(current.getTypicalLevel(), level, current::setTypicalLevel);
            }
        }

        Map<String, Occupation.CredentialRequirementLink> desiredCredentials = o.getCredentials().stream()
                .collect(Collectors.toMap(c -> c.credential().value(), c -> c, (a, b) -> a, LinkedHashMap::new));
        changed |= e.getCredentials().removeIf(c -> !desiredCredentials.containsKey(c.getCredential().getCode()));
        Map<String, OccupationCredentialEntity> currentCredentials = e.getCredentials().stream()
                .collect(Collectors.toMap(c -> c.getCredential().getCode(), c -> c));
        for (Occupation.CredentialRequirementLink c : desiredCredentials.values()) {
            OccupationCredentialEntity current = currentCredentials.get(c.credential().value());
            if (current == null) {
                e.getCredentials().add(new OccupationCredentialEntity(e, credentialByCode.get(c.credential().value()),
                        c.requirement(), c.noteVi()));
                changed = true;
            } else {
                changed |= setIfDifferent(current.getRequirement(), c.requirement(), current::setRequirement);
                changed |= setIfDifferent(current.getNoteVi(), c.noteVi(), current::setNoteVi);
            }
        }
        return changed;
    }

    private boolean syncJobFamilies(OccupationEntity e, Occupation o, Map<String, JobFamilyEntity> familyByCode) {
        List<String> desired = o.getJobFamilies().stream().map(TaxonomyCode::value).toList();
        String primary = desired.getFirst();
        boolean changed = e.getJobFamilies().removeIf(f -> !desired.contains(f.getJobFamily().getCode()));
        Map<String, OccupationJobFamilyEntity> current = e.getJobFamilies().stream()
                .collect(Collectors.toMap(f -> f.getJobFamily().getCode(), f -> f));

        boolean primaryMoved = current.values().stream()
                .anyMatch(f -> f.isPrimaryFamily() && !f.getJobFamily().getCode().equals(primary));
        if (primaryMoved) {
            current.values().forEach(f -> f.setPrimaryFamily(false));
            occupations.flush();
            changed = true;
        }
        for (String code : desired) {
            boolean isPrimary = code.equals(primary);
            OccupationJobFamilyEntity f = current.get(code);
            if (f == null) {
                e.getJobFamilies().add(new OccupationJobFamilyEntity(e, familyByCode.get(code), isPrimary));
                changed = true;
            } else if (f.isPrimaryFamily() != isPrimary) {
                f.setPrimaryFamily(isPrimary);
                changed = true;
            }
        }
        return changed;
    }

    private static Map<String, Map<RelationKey, BigDecimal>> desiredRelations(List<Skill> catalogSkills) {
        Map<String, Map<RelationKey, BigDecimal>> result = new HashMap<>();
        for (Skill s : catalogSkills) {
            for (SkillRelation r : s.getRelations()) {
                put(result, s.getCode().value(), new RelationKey(r.target().value(), r.type()), r.weight());
                if (r.type() == RelationType.RELATED) {
                    put(result, r.target().value(), new RelationKey(s.getCode().value(), r.type()), r.weight());
                }
            }
        }
        return result;
    }

    private static void put(
            Map<String, Map<RelationKey, BigDecimal>> map, String from, RelationKey key, BigDecimal weight
    ) {
        map.computeIfAbsent(from, k -> new LinkedHashMap<>()).put(key, weight);
    }

    private static boolean syncRelations(
            SkillEntity e, Map<RelationKey, BigDecimal> desired, Map<String, SkillEntity> skillByCode
    ) {
        boolean changed = e.getRelations().removeIf(r -> !desired.containsKey(keyOf(r)));
        Map<RelationKey, SkillRelationEntity> current = e.getRelations().stream()
                .collect(Collectors.toMap(TaxonomyCatalogWriterAdapter::keyOf, r -> r));
        for (Map.Entry<RelationKey, BigDecimal> d : desired.entrySet()) {
            SkillRelationEntity r = current.get(d.getKey());
            if (r == null) {
                e.getRelations().add(new SkillRelationEntity(e, skillByCode.get(d.getKey().target()),
                        d.getKey().type(), d.getValue()));
                changed = true;
            } else if (!sameNumber(r.getWeight(), d.getValue())) {
                r.setWeight(d.getValue());
                changed = true;
            }
        }
        return changed;
    }

    private Counter syncExternalRefs(TaxonomyCatalog catalog, Map<String, OccupationEntity> occupationByCode) {
        Counter counter = new Counter("external_ref");
        Map<String, List<ExternalReference>> desiredByOccupation = catalog.externalReferences().stream()
                .filter(r -> r.entityType() == RefEntityType.OCCUPATION)
                .collect(Collectors.groupingBy(r -> r.entityCode().value()));
        for (Occupation o : catalog.occupations()) {
            OccupationEntity e = occupationByCode.get(o.getCode().value());
            Map<String, MatchType> desired = new LinkedHashMap<>();
            desiredByOccupation.getOrDefault(o.getCode().value(), List.of())
                    .forEach(r -> desired.put(r.scheme() + "|" + r.externalCode(), r.matchType()));
            List<ExternalRefEntity> current = externalRefs.findByEntityTypeAndEntityId(RefEntityType.OCCUPATION, e.getId());
            Map<String, ExternalRefEntity> currentByKey = new HashMap<>();
            for (ExternalRefEntity ref : current) {
                String key = ref.getScheme() + "|" + ref.getExternalCode();
                if (!desired.containsKey(key)) {
                    externalRefs.delete(ref);
                    counter.record(false, true);
                } else {
                    currentByKey.put(key, ref);
                }
            }
            for (ExternalReference r : desiredByOccupation.getOrDefault(o.getCode().value(), List.of())) {
                ExternalRefEntity ref = currentByKey.get(r.scheme() + "|" + r.externalCode());
                if (ref == null) {
                    externalRefs.save(new ExternalRefEntity(RefEntityType.OCCUPATION, e.getId(), r.scheme(),
                            r.externalCode(), r.matchType()));
                    counter.record(true, false);
                } else {
                    counter.record(false, setIfDifferent(ref.getMatchType(), r.matchType(), ref::setMatchType));
                }
            }
        }
        return counter;
    }

    private static boolean syncAliases(List<AliasEmbeddable> current, List<Alias> desired) {
        List<String> currentKeys = current.stream().map(a -> a.getAlias() + "|" + a.getNormalized() + "|" + a.getLang()).toList();
        List<String> desiredKeys = desired.stream().map(a -> a.text() + "|" + a.normalized() + "|" + a.lang()).toList();
        if (currentKeys.equals(desiredKeys)) {
            return false;
        }
        current.clear();
        desired.forEach(a -> current.add(new AliasEmbeddable(a.text(), a.normalized(), a.lang())));
        return true;
    }

    private static <T> boolean setIfDifferent(T current, T desired, Consumer<T> setter) {
        if (Objects.equals(current, desired)) {
            return false;
        }
        setter.accept(desired);
        return true;
    }

    private static boolean sameNumber(BigDecimal a, BigDecimal b) {
        return a == null ? b == null : b != null && a.compareTo(b) == 0;
    }

    private static Short toShort(Integer value) {
        return value == null ? null : value.shortValue();
    }

    private static TaxonomyCode code(CodedEntity entity) {
        return entity == null ? null : TaxonomyCode.of(entity.getCode());
    }

    private static RelationKey keyOf(SkillRelationEntity r) {
        return new RelationKey(r.getToSkill().getCode(), r.getType());
    }

    private static <E extends CodedEntity> Map<String, E> byCode(List<E> entities) {
        return entities.stream().collect(Collectors.toMap(CodedEntity::getCode, Function.identity(),
                (a, b) -> a, LinkedHashMap::new));
    }

    private static Set<TaxonomyCode> codes(List<? extends CodedEntity> entities) {
        return entities.stream().map(e -> TaxonomyCode.of(e.getCode())).collect(Collectors.toSet());
    }

    private record RelationKey(String target, RelationType type) {
    }

    private static final class Counter {
        private final String type;
        private int created;
        private int updated;
        private int unchanged;

        Counter(String type) {
            this.type = type;
        }

        void record(boolean isCreated, boolean isChanged) {
            if (isCreated) {
                created++;
            } else if (isChanged) {
                updated++;
            } else {
                unchanged++;
            }
        }

        TaxonomyWriteSummary.Line line() {
            return new TaxonomyWriteSummary.Line(type, created, updated, unchanged);
        }
    }
}
