package com.goatcompass.taxonomy.infrastructure.web.controller;

import com.goatcompass.shared.application.exception.NotFoundException;
import com.goatcompass.shared.infrastructure.web.annotation.ApiMessage;
import com.goatcompass.taxonomy.application.service.TaxonomyBrowseQueryService;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.JobFamilyDetailView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.JobFamilyView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.OccupationDetailView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.SkillDetailView;
import com.goatcompass.taxonomy.application.query.TaxonomyBrowseViews.SkillListItemView;
import com.goatcompass.taxonomy.application.service.TaxonomyQueryService;
import com.goatcompass.taxonomy.infrastructure.web.dto.SkillResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/taxonomy")
@RequiredArgsConstructor
public class TaxonomyController {

    private final TaxonomyBrowseQueryService browse;
    private final TaxonomyQueryService taxonomy;

    @GetMapping("/job-families")
    @ApiMessage("Lấy danh sách lĩnh vực thành công")
    public List<JobFamilyView> jobFamilies() {
        return browse.listJobFamilies();
    }

    @GetMapping("/job-families/{code}")
    @ApiMessage("Lấy lĩnh vực thành công")
    public JobFamilyDetailView jobFamily(@PathVariable String code) throws NotFoundException {
        return browse.getJobFamily(code)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy lĩnh vực '" + code + "'"));
    }

    @GetMapping("/occupations/{code}")
    @ApiMessage("Lấy nghề thành công")
    public OccupationDetailView occupation(@PathVariable String code) throws NotFoundException {
        return browse.getOccupation(code)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nghề '" + code + "'"));
    }

    @GetMapping(value = "/skills", params = "!alias")
    @ApiMessage("Lấy danh sách kỹ năng thành công")
    public List<SkillListItemView> skills(@RequestParam(required = false, defaultValue = "") String q) {
        return browse.searchSkills(q);
    }

    @GetMapping(value = "/skills", params = "alias")
    @ApiMessage("Ánh xạ kỹ năng thành công")
    public List<SkillResponse> resolveSkills(@RequestParam String alias) {
        return taxonomy.resolveSkills(alias).stream().map(SkillResponse::from).toList();
    }

    @GetMapping("/skills/{code}")
    @ApiMessage("Lấy kỹ năng thành công")
    public SkillDetailView skill(@PathVariable String code) throws NotFoundException {
        return browse.getSkill(code)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy kỹ năng '" + code + "'"));
    }
}