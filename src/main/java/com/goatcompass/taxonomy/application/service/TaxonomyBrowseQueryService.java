package com.goatcompass.taxonomy.application.service;

import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.AliasView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.CredentialUseView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.CredentialView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.JobFamilyDetailView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.JobFamilyView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.OccupationDetailView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.OccupationSummaryView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.OccupationUseView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.Ref;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.RelatedSkillView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.RequiredSkillView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.SkillDetailView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.SkillListItemView;
import com.goatcompass.taxonomy.domain.model.aggregate.Credential;
import com.goatcompass.taxonomy.domain.model.aggregate.JobFamily;
import com.goatcompass.taxonomy.domain.model.aggregate.Occupation;
import com.goatcompass.taxonomy.domain.model.aggregate.Skill;
import com.goatcompass.taxonomy.domain.model.enumeration.Importance;
import com.goatcompass.taxonomy.domain.model.enumeration.RelationType;
import com.goatcompass.taxonomy.domain.model.enumeration.Status;
import com.goatcompass.taxonomy.domain.model.valueobject.Alias;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import com.goatcompass.taxonomy.domain.repository.CredentialRepository;
import com.goatcompass.taxonomy.domain.repository.JobFamilyRepository;
import com.goatcompass.taxonomy.domain.repository.OccupationRepository;
import com.goatcompass.taxonomy.domain.repository.SkillRepository;
import com.goatcompass.taxonomy.domain.service.AliasNormalizer;
import java.text.Collator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaxonomyBrowseQueryService {

    private static final Collator VIETNAMESE = Collator.getInstance(Locale.of("vi", "VN"));

    private final JobFamilyRepository jobFamilies;
    private final OccupationRepository occupations;
    private final SkillRepository skills;
    private final CredentialRepository credentials;

    public List<JobFamilyView> listJobFamilies() {
        return visibleFamilies().values().stream()
                .map(f -> new JobFamilyView(f.code().value(), f.nameVi(), f.nameEn(), f.descriptionVi(),
                        (int) occupations.findByJobFamily(f.code()).stream()
                                .filter(o -> o.primaryJobFamily().equals(f.code())).count()))
                .toList();
    }

    public Optional<JobFamilyDetailView> getJobFamily(String code) {
        if (!TaxonomyCode.isValid(code)) {
            return Optional.empty();
        }
        JobFamily family = visibleFamilies().get(code);
        if (family == null) {
            return Optional.empty();
        }
        List<OccupationSummaryView> list = occupations.findByJobFamily(family.code()).stream()
                .filter(o -> o.getStatus() != Status.DEPRECATED)
                .sorted(Comparator.comparing(Occupation::getNameVi, VIETNAMESE))
                .map(o -> new OccupationSummaryView(o.getCode().value(), o.getNameVi(), o.getNameEn(),
                        o.getDescriptionVi(), o.isRegulated(), count(o, Importance.ESSENTIAL),
                        count(o, Importance.OPTIONAL)))
                .toList();
        return Optional.of(new JobFamilyDetailView(family.code().value(), family.nameVi(), family.nameEn(),
                family.descriptionVi(), list));
    }

    public Optional<OccupationDetailView> getOccupation(String code) {
        if (!TaxonomyCode.isValid(code)) {
            return Optional.empty();
        }
        Map<String, JobFamily> visible = visibleFamilies();
        return occupations.findByCode(TaxonomyCode.of(code))
                .filter(o -> visible.containsKey(o.primaryJobFamily().value()))
                .map(o -> toDetail(o, visible.get(o.primaryJobFamily().value())));
    }

    public List<SkillListItemView> searchSkills(String query) {
        String q = AliasNormalizer.normalize(query);
        return skills.findAll().stream()
                .filter(s -> q.isEmpty() || matches(s, q))
                .sorted(Comparator.comparing(Skill::getNameVi, VIETNAMESE))
                .map(s -> new SkillListItemView(s.getCode().value(), s.getNameVi(), s.getNameEn(), s.getKind().name(),
                        s.getAcquisition().name(), s.getWeeksToWorking(), s.getStatus() == Status.DEPRECATED))
                .toList();
    }

    public Optional<SkillDetailView> getSkill(String code) {
        if (!TaxonomyCode.isValid(code)) {
            return Optional.empty();
        }
        Map<String, Skill> all = byCode(skills.findAll(), s -> s.getCode().value());
        Skill skill = all.get(code);
        if (skill == null) {
            return Optional.empty();
        }
        TaxonomyCode self = skill.getCode();
        Map<String, JobFamily> visible = visibleFamilies();

        List<OccupationUseView> usedBy = occupations.findBySkill(self).stream()
                .filter(o -> visible.containsKey(o.primaryJobFamily().value()))
                .flatMap(o -> o.getSkills().stream().filter(r -> r.skill().equals(self))
                        .map(r -> new OccupationUseView(o.getCode().value(), o.getNameVi(),
                                r.importance() == Importance.ESSENTIAL, r.level().value())))
                .sorted(Comparator.comparing(OccupationUseView::essential).reversed()
                        .thenComparing(OccupationUseView::nameVi, VIETNAMESE))
                .toList();
        List<CredentialUseView> provedBy = credentials.findAll().stream()
                .flatMap(c -> c.skills().stream().filter(e -> e.skill().equals(self))
                        .map(e -> new CredentialUseView(c.code().value(), c.nameVi(), c.kind().name(), e.level().value())))
                .toList();

        return Optional.of(new SkillDetailView(self.value(), skill.getNameVi(), skill.getNameEn(),
                skill.getDescriptionVi(), skill.getKind().name(), skill.getAcquisition().name(),
                skill.getWeeksToWorking(), skill.fitsRoadmap(), skill.getStatus() == Status.DEPRECATED,
                skill.getReplacedBy() == null ? null : ref(all, skill.getReplacedBy()),
                skill.getAliases().stream().map(a -> new AliasView(a.text(), a.lang())).toList(),
                refs(all, skill.related(RelationType.BROADER)),
                incoming(all.values(), self, RelationType.BROADER),
                skill.getRelations().stream().filter(r -> r.type() == RelationType.RELATED)
                        .map(r -> new RelatedSkillView(r.target().value(), name(all, r.target()), r.weight()))
                        .toList(),
                refs(all, skill.related(RelationType.PREREQUISITE)),
                incoming(all.values(), self, RelationType.PREREQUISITE),
                usedBy, provedBy));
    }


    private OccupationDetailView toDetail(Occupation o, JobFamily family) {
        Map<String, Skill> skillByCode = byCode(skills.findAll(), s -> s.getCode().value());
        Map<String, Credential> credentialByCode = byCode(credentials.findAll(), c -> c.code().value());
        return new OccupationDetailView(o.getCode().value(), o.getNameVi(), o.getNameEn(), o.getDescriptionVi(),
                new Ref(family.code().value(), family.nameVi()), o.isRegulated(),
                o.getAliases().stream().map(Alias::text).toList(),
                requiredSkills(o, Importance.ESSENTIAL, skillByCode),
                requiredSkills(o, Importance.OPTIONAL, skillByCode),
                o.getCredentials().stream().map(link -> {
                    Credential c = credentialByCode.get(link.credential().value());
                    return new CredentialView(c.code().value(), c.nameVi(), c.nameEn(), c.kind().name(), c.issuer(),
                            c.url(), c.prepWeeks(), c.validityMonths(), link.requirement().name(),
                            link.noteVi() != null ? link.noteVi() : c.noteVi());
                }).toList());
    }

    private static List<RequiredSkillView> requiredSkills(
            Occupation o, Importance importance, Map<String, Skill> skillByCode
    ) {
        return o.getSkills().stream().filter(r -> r.importance() == importance)
                .map(r -> {
                    Skill s = skillByCode.get(r.skill().value());
                    return new RequiredSkillView(s.getCode().value(), s.getNameVi(), s.getNameEn(), s.getKind().name(),
                            s.getAcquisition().name(), s.getWeeksToWorking(), r.level().value());
                })
                .sorted(Comparator.comparingInt(RequiredSkillView::level).reversed())
                .toList();
    }

    private Map<String, JobFamily> visibleFamilies() {
        return jobFamilies.findAll().stream().filter(JobFamily::isVisible)
                .collect(Collectors.toMap(f -> f.code().value(), f -> f, (a, b) -> a, LinkedHashMap::new));
    }

    private static boolean matches(Skill s, String q) {
        return AliasNormalizer.normalize(s.getNameVi()).contains(q)
                || AliasNormalizer.normalize(s.getNameEn()).contains(q)
                || s.getAliases().stream().anyMatch(a -> a.normalized().contains(q));
    }

    private static List<Ref> incoming(java.util.Collection<Skill> all, TaxonomyCode target, RelationType type) {
        return all.stream().filter(s -> s.related(type).contains(target))
                .sorted(Comparator.comparing(Skill::getNameVi, VIETNAMESE))
                .map(s -> new Ref(s.getCode().value(), s.getNameVi()))
                .toList();
    }

    private static List<Ref> refs(Map<String, Skill> all, List<TaxonomyCode> codes) {
        return codes.stream().map(c -> ref(all, c)).toList();
    }

    private static Ref ref(Map<String, Skill> all, TaxonomyCode code) {
        return new Ref(code.value(), name(all, code));
    }

    private static String name(Map<String, Skill> all, TaxonomyCode code) {
        Skill s = all.get(code.value());
        return s == null ? code.value() : s.getNameVi();
    }

    private static int count(Occupation o, Importance importance) {
        return (int) o.getSkills().stream().filter(r -> r.importance() == importance).count();
    }

    private static <T> Map<String, T> byCode(List<T> items, Function<T, String> code) {
        return items.stream().collect(Collectors.toMap(code, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }
}
