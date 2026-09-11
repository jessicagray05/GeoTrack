package com.jessicagray.geotrack.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class SiteInvestigation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(
            message = "Investigation reference is required"
    )
    @Size(
            max = 50,
            message = "Investigation reference must be 50 characters or fewer"
    )
    private String investigationReference;

    @NotBlank(
            message = "Investigation type is required"
    )
    @Size(
            max = 100,
            message = "Investigation type must be 100 characters or fewer"
    )
    private String investigationType;

    @NotBlank(
            message = "Investigation location is required"
    )
    @Size(
            max = 200,
            message = "Investigation location must be 200 characters or fewer"
    )
    private String location;

    @NotBlank(
            message = "Investigation status is required"
    )
    @Size(
            max = 50,
            message = "Investigation status must be 50 characters or fewer"
    )
    private String status;

    @NotBlank(
            message = "Investigation date is required"
    )
    @Size(
            max = 30,
            message = "Investigation date must be 30 characters or fewer"
    )
    private String date;

    @Size(
            max = 1000,
            message = "Investigation notes must be 1000 characters or fewer"
    )
    private String notes;

    /*
     * Every investigation belongs directly to an organisation.
     *
     * This is temporarily nullable while existing GeoTrack
     * investigations are migrated to HESI.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    @JsonIgnore
    private Organisation organisation;

    /*
     * An investigation may optionally be associated
     * with a project.
     *
     * The service layer will ensure that the project
     * belongs to the same organisation.
     */
    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonBackReference
    private Project project;

    public SiteInvestigation() {
    }

    public SiteInvestigation(
            String investigationReference,
            String investigationType,
            String location,
            String status,
            String date,
            String notes) {

        this.investigationReference =
                investigationReference;

        this.investigationType =
                investigationType;

        this.location =
                location;

        this.status =
                status;

        this.date =
                date;

        this.notes =
                notes;
    }

    public Long getId() {
        return id;
    }

    public String getInvestigationReference() {
        return investigationReference;
    }

    public void setInvestigationReference(
            String investigationReference) {

        this.investigationReference =
                investigationReference;
    }

    public String getInvestigationType() {
        return investigationType;
    }

    public void setInvestigationType(
            String investigationType) {

        this.investigationType =
                investigationType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(
            String location) {

        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status) {

        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(
            String date) {

        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(
            String notes) {

        this.notes = notes;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(
            Organisation organisation) {

        this.organisation = organisation;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(
            Project project) {

        this.project = project;
    }

    @JsonProperty("projectId")
    public Long getProjectId() {

        if (project == null) {
            return null;
        }

        return project.getId();
    }
}