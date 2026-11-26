package com.goatcompass.taxonomy.domain.repository;

import com.goatcompass.taxonomy.domain.model.aggregate.JobFamily;
import java.util.List;

public interface JobFamilyRepository {
    List<JobFamily> findAll();
}