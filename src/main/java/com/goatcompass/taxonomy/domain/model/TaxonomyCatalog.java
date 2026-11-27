package com.goatcompass.taxonomy.domain.model;

import com.goatcompass.taxonomy.domain.model.aggregate.Credential;
import com.goatcompass.taxonomy.domain.model.aggregate.JobFamily;
import com.goatcompass.taxonomy.domain.model.aggregate.Occupation;
import com.goatcompass.taxonomy.domain.model.aggregate.Skill;
import com.goatcompass.taxonomy.domain.model.valueobject.ExternalReference;
import java.util.List;

public record TaxonomyCatalog(
        List<JobFamily> jobFamilies, List<Skill> skills, List<Credential> credentials,
        List<Occupation> occupations, List<ExternalReference> externalReferences
) {
    public TaxonomyCatalog {
        jobFamilies = List.copyOf(jobFamilies);
        skills = List.copyOf(skills);
        credentials = List.copyOf(credentials);
        occupations = List.copyOf(occupations);
        externalReferences = List.copyOf(externalReferences);
    }
}
