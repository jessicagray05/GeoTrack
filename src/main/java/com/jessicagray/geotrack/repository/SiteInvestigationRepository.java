package com.jessicagray.geotrack.repository;

import com.jessicagray.geotrack.model.SiteInvestigation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteInvestigationRepository
        extends JpaRepository<SiteInvestigation, Long> {

}