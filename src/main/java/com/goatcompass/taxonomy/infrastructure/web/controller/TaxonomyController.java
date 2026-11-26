package com.goatcompass.taxonomy.infrastructure.web.controller;

import com.goatcompass.shared.application.exception.NotFoundException;
import com.goatcompass.shared.infrastructure.web.annotation.ApiMessage;
import com.goatcompass.taxonomy.api.TaxonomyApi;
import com.goatcompass.taxonomy.application.service.TaxonomyQueryService;
import com.goatcompass.taxonomy.infrastructure.web.dto.JobFamilyResponse;
import com.goatcompass.taxonomy.infrastructure.web.dto.OccupationResponse;
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
    private final TaxonomyQueryService taxonomy;

    @GetMapping("/job-families")
    @ApiMessage("Lấy danh sách lĩnh vực thành công")
    public List<JobFamilyResponse> jobFamilies() {
        return taxonomy.listJobFamilies().stream()
                .filter(TaxonomyApi.JobFamilySummary::visible)
                .map(JobFamilyResponse::from)
                .toList();
    }

    @GetMapping("/skills/{code}")
    @ApiMessage("Lấy kỹ năng thành công")
    public SkillResponse skill(@PathVariable String code) throws NotFoundException {
        return taxonomy.findSkill(code).map(SkillResponse::from)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy kỹ năng '" + code + "'"));
    }

    @GetMapping("/skills")
    @ApiMessage("Ánh xạ kỹ năng thành công")
    public List<SkillResponse> resolveSkills(@RequestParam String alias) {
        return taxonomy.resolveSkills(alias).stream().map(SkillResponse::from).toList();
    }

    @GetMapping("/occupations/{code}")
    @ApiMessage("Lấy nghề thành công")
    public OccupationResponse occupation(@PathVariable String code) throws NotFoundException {
        return taxonomy.findOccupation(code).map(OccupationResponse::from)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nghề '" + code + "'"));
    }
}