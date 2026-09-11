package com.jessicagray.geotrack.repository;

import com.jessicagray.geotrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByUsername(
            String username
    );

    Optional<User> findByUsernameIgnoreCase(
            String username
    );

    Optional<User> findByEmailIgnoreCase(
            String email
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    boolean existsByUsernameIgnoreCase(
            String username
    );

    /*
     * Team Management must always retrieve users
     * through their organisation.
     *
     * This prevents an organisation administrator
     * from receiving users belonging to another
     * GeoTrack company.
     */
    List<User> findAllByOrganisationId(
            Long organisationId
    );

    /*
     * Used when managing one specific team member.
     *
     * Both the user ID and organisation ID must match.
     * There is deliberately no global user lookup in
     * the Team Management flow.
     */
    Optional<User> findByIdAndOrganisationId(
            Long id,
            Long organisationId
    );
}