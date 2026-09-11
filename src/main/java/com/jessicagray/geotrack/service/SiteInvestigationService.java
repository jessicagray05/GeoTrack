package com.jessicagray.geotrack.service;

import com.jessicagray.geotrack.model.Organisation;
import com.jessicagray.geotrack.model.Project;
import com.jessicagray.geotrack.model.SiteInvestigation;
import com.jessicagray.geotrack.model.User;
import com.jessicagray.geotrack.repository.ProjectRepository;
import com.jessicagray.geotrack.repository.SiteInvestigationRepository;
import com.jessicagray.geotrack.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SiteInvestigationService {

    private final SiteInvestigationRepository repository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public SiteInvestigationService(
            SiteInvestigationRepository repository,
            ProjectRepository projectRepository,
            UserRepository userRepository) {

        this.repository = repository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    /**
     * Return only investigations belonging to the
     * organisation of the currently logged-in user.
     */
    public List<SiteInvestigation> getAllInvestigations() {

        Organisation organisation =
                getCurrentOrganisation();

        return repository.findAllByOrganisationId(
                organisation.getId()
        );
    }

    /**
     * Retrieve an investigation only when it belongs
     * to the current user's organisation.
     */
    public Optional<SiteInvestigation> getInvestigationById(
            Long id) {

        Organisation organisation =
                getCurrentOrganisation();

        return repository.findByIdAndOrganisationId(
                id,
                organisation.getId()
        );
    }

    /**
     * New investigations automatically belong to the
     * organisation of the logged-in user.
     *
     * Organisation ownership is never accepted from
     * the browser.
     */
    public SiteInvestigation createInvestigation(
            SiteInvestigation investigation) {

        Organisation organisation =
                getCurrentOrganisation();

        investigation.setOrganisation(organisation);

        /**
         * Project assignment is handled separately by
         * the secure assignment endpoint.
         */
        investigation.setProject(null);

        return repository.save(investigation);
    }

    /**
     * Only an investigation belonging to the current
     * organisation can be updated.
     */
    public SiteInvestigation updateInvestigation(
            Long id,
            SiteInvestigation updatedInvestigation) {

        Organisation organisation =
                getCurrentOrganisation();

        return repository
                .findByIdAndOrganisationId(
                        id,
                        organisation.getId()
                )
                .map(existingInvestigation -> {

                    existingInvestigation
                            .setInvestigationReference(
                                    updatedInvestigation
                                            .getInvestigationReference()
                            );

                    existingInvestigation
                            .setInvestigationType(
                                    updatedInvestigation
                                            .getInvestigationType()
                            );

                    existingInvestigation
                            .setLocation(
                                    updatedInvestigation
                                            .getLocation()
                            );

                    existingInvestigation
                            .setStatus(
                                    updatedInvestigation
                                            .getStatus()
                            );

                    existingInvestigation
                            .setDate(
                                    updatedInvestigation
                                            .getDate()
                            );

                    existingInvestigation
                            .setNotes(
                                    updatedInvestigation
                                            .getNotes()
                            );

                    /**
                     * Organisation and project ownership
                     * are deliberately not copied from the
                     * incoming browser request.
                     */
                    return repository.save(
                            existingInvestigation
                    );
                })
                .orElseThrow(
                        () -> new RuntimeException(
                                "Investigation not found"
                        )
                );
    }

    /**
     * Assign an investigation to a project.
     *
     * Both records must belong to the organisation
     * of the currently logged-in user.
     */
    public SiteInvestigation assignProject(
            Long investigationId,
            Long projectId) {

        Organisation organisation =
                getCurrentOrganisation();

        SiteInvestigation investigation =
                repository
                        .findByIdAndOrganisationId(
                                investigationId,
                                organisation.getId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Investigation not found"
                                )
                        );

        Project project =
                projectRepository
                        .findByIdAndOrganisationId(
                                projectId,
                                organisation.getId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Project not found"
                                )
                        );

        investigation.setProject(project);

        return repository.save(investigation);
    }

    /**
     * Delete only an investigation belonging to the
     * current user's organisation.
     */
    public void deleteInvestigation(Long id) {

        Organisation organisation =
                getCurrentOrganisation();

        SiteInvestigation investigation =
                repository
                        .findByIdAndOrganisationId(
                                id,
                                organisation.getId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Investigation not found"
                                )
                        );

        repository.delete(investigation);
    }

    /**
     * ---------------------------------------------------------
     * CURRENT ORGANISATION
     * ---------------------------------------------------------
     *
     * GeoTrack derives organisation ownership from the
     * authenticated Spring Security account.
     */
    private Organisation getCurrentOrganisation() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "No authenticated GeoTrack user."
            );
        }

        String loginIdentifier =
                authentication.getName();

        User user = userRepository
                .findByEmailIgnoreCase(loginIdentifier)
                .or(() ->
                        userRepository
                                .findByUsernameIgnoreCase(
                                        loginIdentifier
                                )
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Authenticated GeoTrack user "
                                        + "could not be found."
                        )
                );

        if (user.getOrganisation() == null) {

            throw new IllegalStateException(
                    "GeoTrack user is not assigned "
                            + "to an organisation."
            );
        }

        if (!user.getOrganisation().isEnabled()) {

            throw new IllegalStateException(
                    "GeoTrack organisation is disabled."
            );
        }

        return user.getOrganisation();
    }
}