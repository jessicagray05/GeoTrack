package com.jessicagray.geotrack;

import com.jessicagray.geotrack.model.SiteInvestigation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SiteInvestigationTests {

    private static ValidatorFactory validatorFactory;

    private static Validator validator;


    @BeforeAll
    static void setUp() {

        validatorFactory =
                Validation.buildDefaultValidatorFactory();

        validator =
                validatorFactory.getValidator();
    }


    @AfterAll
    static void tearDown() {

        validatorFactory.close();
    }


    @Test
    void shouldAcceptValidInvestigation() {

        SiteInvestigation investigation =
                new SiteInvestigation(
                        "INV-001",
                        "Ground Investigation",
                        "Harlow",
                        "PLANNED",
                        "2026-09-10",
                        "Initial site investigation"
                );

        Set<ConstraintViolation<SiteInvestigation>>
                violations =
                validator.validate(investigation);

        assertTrue(violations.isEmpty());
    }


    @Test
    void shouldRejectBlankInvestigationReference() {

        SiteInvestigation investigation =
                new SiteInvestigation(
                        "",
                        "Ground Investigation",
                        "Harlow",
                        "PLANNED",
                        "2026-09-10",
                        "Notes"
                );

        Set<ConstraintViolation<SiteInvestigation>>
                violations =
                validator.validate(investigation);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation
                                        .getPropertyPath()
                                        .toString()
                                        .equals(
                                                "investigationReference"
                                        )
                        )
        );
    }


    @Test
    void shouldRejectBlankInvestigationType() {

        SiteInvestigation investigation =
                new SiteInvestigation(
                        "INV-001",
                        "",
                        "Harlow",
                        "PLANNED",
                        "2026-09-10",
                        "Notes"
                );

        Set<ConstraintViolation<SiteInvestigation>>
                violations =
                validator.validate(investigation);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation
                                        .getPropertyPath()
                                        .toString()
                                        .equals(
                                                "investigationType"
                                        )
                        )
        );
    }


    @Test
    void shouldRejectBlankLocation() {

        SiteInvestigation investigation =
                new SiteInvestigation(
                        "INV-001",
                        "Ground Investigation",
                        "",
                        "PLANNED",
                        "2026-09-10",
                        "Notes"
                );

        Set<ConstraintViolation<SiteInvestigation>>
                violations =
                validator.validate(investigation);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation
                                        .getPropertyPath()
                                        .toString()
                                        .equals(
                                                "location"
                                        )
                        )
        );
    }


    @Test
    void shouldRejectBlankStatus() {

        SiteInvestigation investigation =
                new SiteInvestigation(
                        "INV-001",
                        "Ground Investigation",
                        "Harlow",
                        "",
                        "2026-09-10",
                        "Notes"
                );

        Set<ConstraintViolation<SiteInvestigation>>
                violations =
                validator.validate(investigation);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation
                                        .getPropertyPath()
                                        .toString()
                                        .equals(
                                                "status"
                                        )
                        )
        );
    }


    @Test
    void shouldRejectBlankDate() {

        SiteInvestigation investigation =
                new SiteInvestigation(
                        "INV-001",
                        "Ground Investigation",
                        "Harlow",
                        "PLANNED",
                        "",
                        "Notes"
                );

        Set<ConstraintViolation<SiteInvestigation>>
                violations =
                validator.validate(investigation);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation
                                        .getPropertyPath()
                                        .toString()
                                        .equals(
                                                "date"
                                        )
                        )
        );
    }


    @Test
    void shouldRejectOversizedNotes() {

        String longNotes =
                "A".repeat(1001);

        SiteInvestigation investigation =
                new SiteInvestigation(
                        "INV-001",
                        "Ground Investigation",
                        "Harlow",
                        "PLANNED",
                        "2026-09-10",
                        longNotes
                );

        Set<ConstraintViolation<SiteInvestigation>>
                violations =
                validator.validate(investigation);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation
                                        .getPropertyPath()
                                        .toString()
                                        .equals(
                                                "notes"
                                        )
                        )
        );
    }
}