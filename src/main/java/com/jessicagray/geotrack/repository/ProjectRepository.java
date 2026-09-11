package com.jessicagray.geotrack.repository;

import com.jessicagray.geotrack.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository
        extends JpaRepository<Project, Long> {

    List<Project> findAllByOrganisationId(Long organisationId);

    Optional<Project> findByIdAndOrganisationId(
            Long id,
            Long organisationId
    );
}