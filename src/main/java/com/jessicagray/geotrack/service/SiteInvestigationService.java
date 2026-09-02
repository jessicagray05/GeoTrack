package com.jessicagray.geotrack.service;

import com.jessicagray.geotrack.model.Project;
import com.jessicagray.geotrack.model.SiteInvestigation;
import com.jessicagray.geotrack.repository.ProjectRepository;
import com.jessicagray.geotrack.repository.SiteInvestigationRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SiteInvestigationService {

    private final SiteInvestigationRepository repository;

    private final ProjectRepository projectRepository;


    public SiteInvestigationService(
            SiteInvestigationRepository repository,
            ProjectRepository projectRepository) {

        this.repository = repository;

        this.projectRepository = projectRepository;

    }


    public List<SiteInvestigation> getAllInvestigations() {

        return repository.findAll();

    }


    public Optional<SiteInvestigation> getInvestigationById(
            Long id) {

        return repository.findById(id);

    }


    public SiteInvestigation createInvestigation(
            SiteInvestigation investigation) {

        return repository.save(investigation);

    }


    public SiteInvestigation updateInvestigation(
            Long id,
            SiteInvestigation updatedInvestigation) {

        return repository.findById(id)

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


    public SiteInvestigation assignProject(
            Long investigationId,
            Long projectId) {

        SiteInvestigation investigation =
                repository.findById(investigationId)

                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Investigation not found"
                                )
                        );


        Project project =
                projectRepository.findById(projectId)

                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Project not found"
                                )
                        );


        investigation.setProject(project);


        return repository.save(investigation);

    }


    public void deleteInvestigation(Long id) {

        repository.deleteById(id);

    }

}