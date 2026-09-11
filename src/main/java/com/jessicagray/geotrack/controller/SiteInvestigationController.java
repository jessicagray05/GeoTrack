package com.jessicagray.geotrack.controller;

import com.jessicagray.geotrack.model.SiteInvestigation;
import com.jessicagray.geotrack.service.SiteInvestigationService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investigations")
public class SiteInvestigationController {

    private final SiteInvestigationService service;

    public SiteInvestigationController(
            SiteInvestigationService service) {

        this.service = service;
    }

    @GetMapping
    public List<SiteInvestigation> getAllInvestigations() {

        return service.getAllInvestigations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SiteInvestigation> getInvestigationById(
            @PathVariable Long id) {

        return service.getInvestigationById(id)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }

    @PostMapping
    public SiteInvestigation createInvestigation(
            @Valid @RequestBody SiteInvestigation investigation) {

        return service.createInvestigation(
                investigation
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SiteInvestigation> updateInvestigation(
            @PathVariable Long id,
            @Valid @RequestBody SiteInvestigation investigation) {

        try {

            return ResponseEntity.ok(
                    service.updateInvestigation(
                            id,
                            investigation
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{investigationId}/project/{projectId}")
    public ResponseEntity<SiteInvestigation> assignProject(
            @PathVariable Long investigationId,
            @PathVariable Long projectId) {

        try {

            return ResponseEntity.ok(
                    service.assignProject(
                            investigationId,
                            projectId
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvestigation(
            @PathVariable Long id) {

        try {

            service.deleteInvestigation(id);

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();
        }
    }
}