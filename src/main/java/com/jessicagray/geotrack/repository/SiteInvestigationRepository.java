package com.jessicagray.geotrack.repository;

import com.jessicagray.geotrack.model.SiteInvestigation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SiteInvestigationRepository
        extends JpaRepository<SiteInvestigation, Long> {

    List<SiteInvestigation>
    findAllByOrganisationId(Long organisationId);

    Optional<SiteInvestigation>
    findByIdAndOrganisationId(
            Long id,
            Long organisationId
    );
}