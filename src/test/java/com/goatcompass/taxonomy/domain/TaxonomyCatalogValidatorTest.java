package com.goatcompass.taxonomy.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.goatcompass.taxonomy.domain.model.TaxonomyCatalog;
import com.goatcompass.taxonomy.domain.model.TaxonomyCodes;
import com.goatcompass.taxonomy.domain.model.aggregate.JobFamily;
import com.goatcompass.taxonomy.domain.model.aggregate.Occupation;
import com.goatcompass.taxonomy.domain.model.aggregate.Skill;
import com.goatcompass.taxonomy.domain.model.enumeration.Acquisition;
import com.goatcompass.taxonomy.domain.model.enumeration.CredentialKind;
import com.goatcompass.taxonomy.domain.model.enumeration.CredentialRequirement;
import com.goatcompass.taxonomy.domain.model.enumeration.Importance;
import com.goatcompass.taxonomy.domain.model.enumeration.RelationType;
import com.goatcompass.taxonomy.domain.model.enumeration.SkillKind;
import com.goatcompass.taxonomy.domain.model.enumeration.Status;
import com.goatcompass.taxonomy.domain.model.aggregate.Credential;
import com.goatcompass.taxonomy.domain.model.valueobject.ProficiencyLevel;
import com.goatcompass.taxonomy.domain.model.valueobject.SkillRelation;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import com.goatcompass.taxonomy.domain.service.TaxonomyCatalogValidator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Unit test kiểm tra toàn cục catalog: không cần Spring hay DB. */
class TaxonomyCatalogValidatorTest {

    private static final JobFamily MARKETING = JobFamily.builder()
            .code(code("marketing")).nameVi("Marketing").nameEn("Marketing").status(Status.ACTIVE).build();

    @Test
    void validCatalogHasNoErrors() {
        var result = validate(catalog(List.of(skill("meta-ads"), skill("paid-advertising")), List.of(occupation("ads", "meta-ads"))));
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void reportsBrokenReferencesAndCycles() {
        Skill a = skill("a", new SkillRelation(RelationType.BROADER, code("b"), null));
        Skill b = skill("b", new SkillRelation(RelationType.BROADER, code("a"), null),
                new SkillRelation(RelationType.PREREQUISITE, code("khong-co"), null));
        var result = validate(catalog(List.of(a, b), List.of(occupation("ads", "missing-skill"))));

        assertThat(result.errors())
                .anyMatch(e -> e.contains("Chu trình BROADER"))
                .anyMatch(e -> e.contains("'khong-co'"))
                .anyMatch(e -> e.contains("Nghề ads") && e.contains("'missing-skill'"));
    }

    @Test
    void relatedMustBeDeclaredOnOneSideOnly() {
        Skill a = skill("a", new SkillRelation(RelationType.RELATED, code("b"), new BigDecimal("0.5")));
        Skill b = skill("b", new SkillRelation(RelationType.RELATED, code("a"), new BigDecimal("0.5")));
        assertThat(validate(catalog(List.of(a, b), List.of())).errors())
                .anyMatch(e -> e.contains("RELATED") && e.contains("cả hai phía"));
    }

    @Test
    void codesMissingFromSourceAreNotSilentlyDeleted() {
        TaxonomyCodes inStore = new TaxonomyCodes(Set.of(code("marketing")), Set.of(code("meta-ads"), code("old-skill")),
                Set.of(), Set.of());
        var result = TaxonomyCatalogValidator.validate(catalog(List.of(skill("meta-ads")), List.of()), inStore);
        assertThat(result.errors()).singleElement().asString().contains("old-skill", "DEPRECATED");
    }

    @Test
    void legallyRequiredCredentialNeedsNote() {
        Credential cpa = Credential.builder().code(code("cpa")).nameVi("CPA").nameEn("CPA")
                .kind(CredentialKind.LICENSE).status(Status.ACTIVE).build();
        Occupation auditor = Occupation.builder().code(code("auditor")).nameVi("x").nameEn("x")
                .jobFamily(code("marketing"))
                .credential(new Occupation.CredentialRequirementLink(code("cpa"), CredentialRequirement.LEGALLY_REQUIRED, null))
                .build();
        var catalog = new TaxonomyCatalog(List.of(MARKETING), List.of(), List.of(cpa), List.of(auditor), List.of());
        assertThat(validate(catalog).errors()).anyMatch(e -> e.contains("LEGALLY_REQUIRED") && e.contains("note_vi"));
    }

    private static TaxonomyCatalogValidator.Result validate(TaxonomyCatalog catalog) {
        return TaxonomyCatalogValidator.validate(catalog, TaxonomyCodes.empty());
    }

    private static TaxonomyCatalog catalog(List<Skill> skills, List<Occupation> occupations) {
        return new TaxonomyCatalog(List.of(MARKETING), skills, List.of(), occupations, List.of());
    }

    private static Skill skill(String code, SkillRelation... relations) {
        return Skill.builder().code(code(code)).nameVi(code).nameEn(code)
                .kind(SkillKind.TECHNICAL).acquisition(Acquisition.QUICK).weeksToWorking(2)
                .relations(List.of(relations)).build();
    }

    private static Occupation occupation(String code, String skill) {
        return Occupation.builder().code(code(code)).nameVi(code).nameEn(code).jobFamily(code("marketing"))
                .skill(new Occupation.SkillRequirement(code(skill), Importance.ESSENTIAL, ProficiencyLevel.of(2)))
                .build();
    }

    private static TaxonomyCode code(String value) {
        return TaxonomyCode.of(value);
    }
}
