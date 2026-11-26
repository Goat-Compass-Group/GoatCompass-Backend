package com.goatcompass.taxonomy.application.service;

import com.goatcompass.taxonomy.api.TaxonomyApi;
import com.goatcompass.taxonomy.domain.model.enumeration.Importance;
import com.goatcompass.taxonomy.domain.model.aggregate.JobFamily;
import com.goatcompass.taxonomy.domain.model.aggregate.Occupation;
import com.goatcompass.taxonomy.domain.model.aggregate.Skill;
import com.goatcompass.taxonomy.domain.model.enumeration.Status;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import com.goatcompass.taxonomy.domain.repository.JobFamilyRepository;
import com.goatcompass.taxonomy.domain.repository.OccupationRepository;
import com.goatcompass.taxonomy.domain.repository.SkillRepository;
import com.goatcompass.taxonomy.domain.service.AliasNormalizer;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaxonomyQueryService implements TaxonomyApi {
    private final SkillRepository skills;
    private final OccupationRepository occupations;
    private final JobFamilyRepository jobFamilies;

    @Override
    public Optional<SkillSummary> findSkill(String code) {
        if (!TaxonomyCode.isValid(code)) {
            return Optional.empty();
        }
        return skills.findByCode(TaxonomyCode.of(code)).map(TaxonomyQueryService::toSummary);
    }

    @Override
    public List<SkillSummary> resolveSkills(String text) {
        String normalized = AliasNormalizer.normalize(text);
        if (normalized.isEmpty()) {
            return List.of();
        }
        return skills.findByNormalizedAlias(normalized).stream().map(TaxonomyQueryService::toSummary).toList();
    }

    @Override
    public Optional<OccupationProfile> findOccupation(String code) {
        if (!TaxonomyCode.isValid(code)) {
            return Optional.empty();
        }
        return occupations.findByCode(TaxonomyCode.of(code)).map(TaxonomyQueryService::toProfile);
    }

    @Override
    public List<JobFamilySummary> listJobFamilies() {
        return jobFamilies.findAll().stream().map(TaxonomyQueryService::toSummary).toList();
    }

    private static SkillSummary toSummary(Skill s) {
        return new SkillSummary(
                s.getCode().value(), s.getNameVi(), s.getNameEn(), s.getKind().name(),
                s.getAcquisition().name(), s.getWeeksToWorking(), s.fitsRoadmap(),
                s.getStatus() == Status.DEPRECATED, s.getReplacedBy() == null ? null : s.getReplacedBy().value()
        );
    }

    private static OccupationProfile toProfile(Occupation o) {
        return new OccupationProfile(
                o.getCode().value(), o.getNameVi(), o.getNameEn(), o.primaryJobFamily().value(),
                o.isRegulated(),
                o.getSkills().stream()
                        .map(r -> new OccupationProfile.RequiredSkill(
                                r.skill().value(), r.importance() == Importance.ESSENTIAL, r.level().value()))
                        .toList(),
                o.getCredentials().stream()
                        .map(c -> new OccupationProfile.RequiredCredential(
                                c.credential().value(), c.requirement().name(), c.noteVi()))
                        .toList()
        );
    }

    private static JobFamilySummary toSummary(JobFamily f) {
        return new JobFamilySummary(f.code().value(), f.nameVi(), f.nameEn(), f.isVisible());
    }
}