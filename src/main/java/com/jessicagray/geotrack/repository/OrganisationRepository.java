package com.jessicagray.geotrack.repository;

import com.jessicagray.geotrack.model.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganisationRepository
        extends JpaRepository<Organisation, Long> {

    Optional<Organisation> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}