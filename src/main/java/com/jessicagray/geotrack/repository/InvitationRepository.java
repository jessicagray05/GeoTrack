package com.jessicagray.geotrack.repository;

import com.jessicagray.geotrack.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository
        extends JpaRepository<Invitation, Long> {

    /*
     * Returns invitations belonging only to the
     * currently authenticated organisation.
     */
    List<Invitation> findAllByOrganisationId(
            Long organisationId
    );

    /*
     * Finds one invitation only when both its ID
     * and organisation match.
     *
     * This prevents one company's administrator
     * from accessing another company's invitation.
     */
    Optional<Invitation> findByIdAndOrganisationId(
            Long id,
            Long organisationId
    );

    /*
     * Used when somebody opens an invitation link.
     *
     * Only the SHA-256 hash is stored in the database,
     * never the raw invitation token.
     */
    Optional<Invitation> findByTokenHash(
            String tokenHash
    );

    /*
     * Prevents multiple active invitations being
     * created for the same email inside one company.
     */
    boolean existsByEmailIgnoreCaseAndOrganisationIdAndAcceptedFalse(
            String email,
            Long organisationId
    );
}