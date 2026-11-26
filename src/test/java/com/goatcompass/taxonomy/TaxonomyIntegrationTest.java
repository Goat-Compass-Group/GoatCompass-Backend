package com.goatcompass.taxonomy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goatcompass.TestcontainersConfiguration;
import com.goatcompass.taxonomy.api.TaxonomyApi;
import com.goatcompass.taxonomy.domain.model.enumeration.Acquisition;
import com.goatcompass.taxonomy.domain.model.enumeration.SkillKind;
import com.goatcompass.taxonomy.domain.service.AliasNormalizer;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.SkillEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.embeddable.AliasEmbeddable;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Chạy xuyên các tầng trên Postgres thật: controller → application service → port → JPA adapter → DB.
 * Mỗi test chạy trong transaction và rollback.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaxonomyIntegrationTest {

    @Autowired
    TaxonomyApi taxonomyApi;

    @Autowired
    MockMvc mvc;

    @Autowired
    EntityManager em;

    @BeforeEach
    void seed() {
        SkillEntity metaAds = new SkillEntity("meta-ads", "Quảng cáo Meta", "Meta Ads", SkillKind.TECHNICAL, Acquisition.QUICK);
        metaAds.setWeeksToWorking((short) 3);
        for (String alias : new String[] {"chạy ads fb", "facebook ads"}) {
            metaAds.getAliases().add(new AliasEmbeddable(alias, AliasNormalizer.normalize(alias), "vi"));
        }
        em.persist(metaAds);
        em.flush();
        em.clear();
    }

    @Test
    void resolvesSkillFromMessyJdText() {
        assertThat(taxonomyApi.resolveSkills("Chạy ADS  FB"))
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.code()).isEqualTo("meta-ads");
                    assertThat(s.fitsRoadmap()).isTrue();
                });
        assertThat(taxonomyApi.resolveSkills("không có kỹ năng này")).isEmpty();
    }

    @Test
    void restEndpointsReturnSkillOrNotFound() throws Exception {
        mvc.perform(get("/api/v1/taxonomy/skills/meta-ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Lấy kỹ năng thành công"))
                .andExpect(jsonPath("$.data.nameVi").value("Quảng cáo Meta"))
                .andExpect(jsonPath("$.data.weeksToWorking").value(3));
        mvc.perform(get("/api/v1/taxonomy/skills").param("alias", "facebook ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("meta-ads"));
        mvc.perform(get("/api/v1/taxonomy/skills/khong-ton-tai"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.error").value("Không tìm thấy"));
    }
}
