package com.goatcompass.taxonomy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.goatcompass.taxonomy.domain.model.enumeration.Acquisition;
import com.goatcompass.taxonomy.domain.model.aggregate.Skill;
import com.goatcompass.taxonomy.domain.model.enumeration.SkillKind;
import com.goatcompass.taxonomy.domain.model.enumeration.Status;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import com.goatcompass.taxonomy.domain.service.AliasNormalizer;
import org.junit.jupiter.api.Test;

/** Unit test tầng domain: không cần Spring hay DB. */
class TaxonomyDomainTest {

    @Test
    void normalizerRemovesVietnameseDiacriticsAndKeepsTechSymbols() {
        assertThat(AliasNormalizer.normalize("  Chạy ADS   Facebook! ")).isEqualTo("chay ads facebook");
        assertThat(AliasNormalizer.normalize("Đo lường chuyển đổi")).isEqualTo("do luong chuyen doi");
        assertThat(AliasNormalizer.normalize("C++, C# và .NET")).isEqualTo("c++ c# va .net");
        assertThat(AliasNormalizer.normalize(null)).isEmpty();
    }

    @Test
    void taxonomyCodeRejectsBadFormat() {
        assertThat(TaxonomyCode.of("meta-ads").value()).isEqualTo("meta-ads");
        assertThat(TaxonomyCode.isValid("Meta Ads")).isFalse();
        assertThatThrownBy(() -> TaxonomyCode.of("meta_ads")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void quickSkillMustFitFourWeekRoadmap() {
        assertThatThrownBy(() -> skill(Acquisition.QUICK, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThat(skill(Acquisition.QUICK, 3).fitsRoadmap()).isTrue();
        assertThat(skill(Acquisition.EXTENDED, 12).fitsRoadmap()).isFalse();
    }

    @Test
    void deprecatePointsToReplacement() {
        Skill ua = skill(Acquisition.QUICK, 2);
        ua.deprecate(TaxonomyCode.of("google-analytics"));
        assertThat(ua.getStatus()).isEqualTo(Status.DEPRECATED);
        assertThat(ua.getReplacedBy().value()).isEqualTo("google-analytics");
        assertThatThrownBy(() -> ua.deprecate(ua.getCode())).isInstanceOf(IllegalArgumentException.class);
    }

    private static Skill skill(Acquisition acquisition, int weeks) {
        return Skill.builder()
                .code(TaxonomyCode.of("universal-analytics"))
                .nameVi("Universal Analytics").nameEn("Universal Analytics")
                .kind(SkillKind.TOOL).acquisition(acquisition).weeksToWorking(weeks)
                .build();
    }
}
