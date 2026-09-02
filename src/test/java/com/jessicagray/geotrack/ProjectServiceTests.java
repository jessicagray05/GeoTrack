package com.jessicagray.geotrack;

import com.jessicagray.geotrack.model.Project;
import com.jessicagray.geotrack.repository.ProjectRepository;
import com.jessicagray.geotrack.service.ProjectService;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceTests {

    @Test
    void shouldGetAllProjects() {

        ProjectRepository projectRepository =
                mock(ProjectRepository.class);

        ProjectService projectService =
                new ProjectService(projectRepository);

        Project project = new Project();

        when(projectRepository.findAll())
                .thenReturn(List.of(project));

        List<Project> result =
                projectService.getAllProjects();

        assertEquals(1, result.size());

        verify(projectRepository)
                .findAll();
    }


    @Test
    void shouldGetProjectById() {

        ProjectRepository projectRepository =
                mock(ProjectRepository.class);

        ProjectService projectService =
                new ProjectService(projectRepository);

        Project project = new Project();

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        Optional<Project> result =
                projectService.getProjectById(1L);

        assertTrue(result.isPresent());

        verify(projectRepository)
                .findById(1L);
    }


    @Test
    void shouldReturnEmptyWhenProjectDoesNotExist() {

        ProjectRepository projectRepository =
                mock(ProjectRepository.class);

        ProjectService projectService =
                new ProjectService(projectRepository);

        when(projectRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Project> result =
                projectService.getProjectById(99L);

        assertTrue(result.isEmpty());

        verify(projectRepository)
                .findById(99L);
    }


    @Test
    void shouldCreateProject() {

        ProjectRepository projectRepository =
                mock(ProjectRepository.class);

        ProjectService projectService =
                new ProjectService(projectRepository);

        Project project = new Project();

        when(projectRepository.save(project))
                .thenReturn(project);

        Project result =
                projectService.createProject(project);

        assertNotNull(result);

        assertSame(project, result);

        verify(projectRepository)
                .save(project);
    }


    @Test
    void shouldDeleteProject() {

        ProjectRepository projectRepository =
                mock(ProjectRepository.class);

        ProjectService projectService =
                new ProjectService(projectRepository);

        projectService.deleteProject(1L);

        verify(projectRepository)
                .deleteById(1L);
    }


    @Test
    void shouldThrowExceptionWhenUpdatingMissingProject() {

        ProjectRepository projectRepository =
                mock(ProjectRepository.class);

        ProjectService projectService =
                new ProjectService(projectRepository);

        Project updatedProject = new Project();

        when(projectRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> projectService.updateProject(
                        99L,
                        updatedProject
                )
        );

        verify(projectRepository)
                .findById(99L);
    }
}