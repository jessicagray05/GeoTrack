package com.jessicagray.geotrack.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Project reference is required")
    private String projectReference;

    @NotBlank(message = "Project name is required")
    private String projectName;

    @NotBlank(message = "Location name is required")
    private String location;

    @NotBlank(message = "Client name is required")
    private String clientName;

    @NotBlank(message = "Status is required")
    private String status;

    private String description;

    private LocalDate startDate;

    private LocalDate targetCompletionDate;

    @OneToMany(mappedBy = "project")
    @JsonManagedReference
    private List<SiteInvestigation> investigations =
            new ArrayList<>();


    public Project() {
    }


    public Project(
            String projectReference,
            String projectName,
            String location,
            String clientName,
            String status,
            String description) {

        this.projectReference = projectReference;
        this.projectName = projectName;
        this.location = location;
        this.clientName = clientName;
        this.status = status;
        this.description = description;
    }


    public Long getId() {
        return id;
    }


    public String getProjectReference() {
        return projectReference;
    }

    public void setProjectReference(
            String projectReference) {

        this.projectReference = projectReference;
    }


    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(
            String projectName) {

        this.projectName = projectName;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(
            String location) {

        this.location = location;
    }


    public String getClientName() {
        return clientName;
    }

    public void setClientName(
            String clientName) {

        this.clientName = clientName;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status) {

        this.status = status;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description) {

        this.description = description;
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(
            LocalDate startDate) {

        this.startDate = startDate;
    }


    public LocalDate getTargetCompletionDate() {
        return targetCompletionDate;
    }

    public void setTargetCompletionDate(
            LocalDate targetCompletionDate) {

        this.targetCompletionDate =
                targetCompletionDate;
    }


    public String getDeadlineStatus() {

        if (targetCompletionDate == null) {
            return "NO_DEADLINE";
        }

        if (LocalDate.now().isAfter(targetCompletionDate)) {
            return "OVERDUE";
        }

        if (!LocalDate.now().plusDays(7)
                .isBefore(targetCompletionDate)) {

            return "DUE_SOON";
        }

        return "ON_TRACK";
    }


    public List<SiteInvestigation> getInvestigations() {

        return investigations;
    }

    public void setInvestigations(
            List<SiteInvestigation> investigations) {

        this.investigations = investigations;
    }
}