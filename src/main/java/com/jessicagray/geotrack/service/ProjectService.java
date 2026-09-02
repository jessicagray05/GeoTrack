package com.jessicagray.geotrack.service;

import com.jessicagray.geotrack.model.Project;
import com.jessicagray.geotrack.repository.ProjectRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;


    public ProjectService(
            ProjectRepository projectRepository) {

        this.projectRepository =
                projectRepository;
    }


    public List<Project> getAllProjects() {

        return projectRepository.findAll();
    }


    public Optional<Project> getProjectById(Long id) {

        return projectRepository.findById(id);
    }


    public Project createProject(Project project) {

        return projectRepository.save(project);
    }


    public Project updateProject(
            Long id,
            Project updatedProject) {

        return projectRepository.findById(id)

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
                            updatedProject.getTargetCompletionDate()
                    );

                    return projectRepository.save(project);

                })

                .orElseThrow(
                        () -> new RuntimeException(
                                "Project not found"
                        )
                );
    }


    public void deleteProject(Long id) {

        projectRepository.deleteById(id);
    }
}