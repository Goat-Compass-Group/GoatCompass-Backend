package com.goatcompass.taxonomy;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goatcompass.TestcontainersConfiguration;
import com.goatcompass.importer.application.service.DataImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** API duyệt taxonomy công khai, chạy trên bộ dữ liệu thật (nạp trong transaction, rollback sau mỗi test). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaxonomyBrowseApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    DataImportService dataImport;

    @BeforeEach
    void loadData() throws Exception {
        dataImport.importTaxonomy(false);
    }

    @Test
    void guestSeesOnlyActiveJobFamilies() throws Exception {
        mvc.perform(get("/api/v1/taxonomy/job-families"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].code").value("marketing"))
                .andExpect(jsonPath("$.data[0].occupationCount").value(7));
        mvc.perform(get("/api/v1/taxonomy/job-families/accounting-finance")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/taxonomy/occupations/statutory-auditor")).andExpect(status().isNotFound());
    }

    @Test
    void occupationDetailCarriesSkillAndCredentialNames() throws Exception {
        mvc.perform(get("/api/v1/taxonomy/occupations/digital-marketing-specialist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobFamily.code").value("marketing"))
                .andExpect(jsonPath("$.data.essentialSkills[*].code", hasItem("meta-ads")))
                .andExpect(jsonPath("$.data.essentialSkills[0].nameVi").isNotEmpty())
                .andExpect(jsonPath("$.data.credentials[*].kind", hasItem("CERTIFICATE")));
    }

    @Test
    void skillSearchIgnoresDiacriticsAndDetailShowsRelations() throws Exception {
        mvc.perform(get("/api/v1/taxonomy/skills").param("q", "quang cao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].code", hasItem("meta-ads")));
        mvc.perform(get("/api/v1/taxonomy/skills/meta-ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.broader[*].code", hasItem("paid-advertising")))
                .andExpect(jsonPath("$.data.related[*].code", hasItem("google-ads")))
                .andExpect(jsonPath("$.data.occupations[*].code", hasItem("digital-marketing-specialist")))
                .andExpect(jsonPath("$.data.credentials[*].code", hasItem("meta-certified-media-buying-professional")));
    }
}
