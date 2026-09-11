package com.jessicagray.geotrack.service;

import com.jessicagray.geotrack.model.Organisation;
import com.jessicagray.geotrack.model.Project;
import com.jessicagray.geotrack.model.User;
import com.jessicagray.geotrack.repository.ProjectRepository;
import com.jessicagray.geotrack.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository) {

        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    /*
     * Return only projects belonging to the organisation
     * of the currently logged-in user.
     */
    public List<Project> getAllProjects() {

        Organisation organisation =
                getCurrentOrganisation();

        return projectRepository
                .findAllByOrganisationId(
                        organisation.getId()
                );
    }

    /*
     * A project can only be retrieved when it belongs
     * to the current user's organisation.
     */
    public Optional<Project> getProjectById(Long id) {

        Organisation organisation =
                getCurrentOrganisation();

        return projectRepository
                .findByIdAndOrganisationId(
                        id,
                        organisation.getId()
                );
    }

    /*
     * New projects automatically belong to the
     * organisation of the logged-in user.
     *
     * The browser does not choose the organisation.
     */
    public Project createProject(Project project) {

        Organisation organisation =
                getCurrentOrganisation();

        project.setOrganisation(organisation);

        return projectRepository.save(project);
    }

    /*
     * Only a project belonging to the current user's
     * organisation can be updated.
     */
    public Project updateProject(
            Long id,
            Project updatedProject) {

        Organisation organisation =
                getCurrentOrganisation();

        return projectRepository
                .findByIdAndOrganisationId(
                        id,
                        organisation.getId()
                )
                .map(project -> {

                    project.setProjectName(
                            updatedProject.getProjectName()
                    );

                    project.setLocation(
                            updatedProject.getLocation()
                    );

                    project.setClientName(
                            updatedProject.getClientName()
                    );

                    project.setStatus(
                            updatedProject.getStatus()
                    );

                    project.setDescription(
                            updatedProject.getDescription()
                    );

                    project.setStartDate(
                            updatedProject.getStartDate()
                    );

                    project.setTargetCompletionDate(
                            updatedProject
                                    .getTargetCompletionDate()
                    );

                    /*
                     * Do not copy organisation information
                     * from the incoming request.
                     *
                     * Existing ownership remains unchanged.
                     */
                    return projectRepository.save(project);
                })
                .orElseThrow(
                        () -> new RuntimeException(
                                "Project not found"
                        )
                );
    }

    /*
     * A project can only be deleted when it belongs
     * to the current user's organisation.
     */
    public void deleteProject(Long id) {

        Organisation organisation =
                getCurrentOrganisation();

        Project project = projectRepository
                .findByIdAndOrganisationId(
                        id,
                        organisation.getId()
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "Project not found"
                        )
                );

        projectRepository.delete(project);
    }

    /*
     * ---------------------------------------------------------
     * CURRENT ORGANISATION
     * ---------------------------------------------------------
     *
     * GeoTrack determines company ownership from the
     * authenticated Spring Security account.
     *
     * organisationId is never accepted from the browser.
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