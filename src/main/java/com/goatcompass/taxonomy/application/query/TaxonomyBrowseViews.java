package com.goatcompass.taxonomy.application.query;

import java.math.BigDecimal;
import java.util.List;

public final class TaxonomyBrowseViews {

    private TaxonomyBrowseViews() {
    }

    public record JobFamilyView(
            String code, String nameVi, String nameEn, String descriptionVi, int occupationCount
    ) {
    }

    public record JobFamilyDetailView(
            String code, String nameVi, String nameEn, String descriptionVi,
            List<OccupationSummaryView> occupations
    ) {
    }

    public record OccupationSummaryView(
            String code, String nameVi, String nameEn, String descriptionVi,
            boolean regulated, int essentialSkillCount, int optionalSkillCount
    ) {
    }

    public record OccupationDetailView(
            String code, String nameVi, String nameEn, String descriptionVi,
            Ref jobFamily, boolean regulated, List<String> aliases,
            List<RequiredSkillView> essentialSkills, List<RequiredSkillView> optionalSkills,
            List<CredentialView> credentials
    ) {
    }

    public record RequiredSkillView(
            String code, String nameVi, String nameEn, String kind, String acquisition,
            Integer weeksToWorking, int level
    ) {
    }

    public record CredentialView(
            String code, String nameVi, String nameEn, String kind, String issuer, String url,
            Integer prepWeeks, Integer validityMonths, String requirement, String noteVi
    ) {
    }

    public record SkillListItemView(
            String code, String nameVi, String nameEn, String kind, String acquisition,
            Integer weeksToWorking, boolean deprecated
    ) {
    }

    public record SkillDetailView(
            String code, String nameVi, String nameEn, String descriptionVi, String kind,
            String acquisition, Integer weeksToWorking, boolean fitsRoadmap, boolean deprecated,
            Ref replacedBy, List<AliasView> aliases, List<Ref> broader, List<Ref> narrower,
            List<RelatedSkillView> related, List<Ref> prerequisites, List<Ref> prerequisiteFor,
            List<OccupationUseView> occupations, List<CredentialUseView> credentials
    ) {
    }

    public record AliasView(String text, String lang) {
    }

    public record RelatedSkillView(String code, String nameVi, BigDecimal weight) {
    }

    public record OccupationUseView(String code, String nameVi, boolean essential, int level) {
    }

    public record CredentialUseView(String code, String nameVi, String kind, int level) {
    }

    public record Ref(String code, String nameVi) {
    }
}
