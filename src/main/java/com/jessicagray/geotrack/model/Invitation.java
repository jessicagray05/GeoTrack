package com.jessicagray.geotrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "invitations")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * The organisation the invited person will join.
     *
     * This value will always be assigned server-side
     * from the administrator who creates the invitation.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    /*
     * Invitations currently create STAFF accounts.
     *
     * Keeping the role on the invitation allows us to
     * support additional organisation roles later.
     */
    @Column(nullable = false)
    private String role = "STAFF";

    /*
     * We will generate a cryptographically random token
     * when the invitation is created.
     *
     * The raw token will eventually be sent in the
     * invitation link. Only its hash will be stored here.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean accepted = false;

    private LocalDateTime acceptedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt =
            LocalDateTime.now();

    public Invitation() {
    }

    public Long getId() {
        return id;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(
            Organisation organisation
    ) {
        this.organisation = organisation;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(
            String fullName
    ) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(
            String email
    ) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(
            String role
    ) {
        this.role = role;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(
            String tokenHash
    ) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(
            LocalDateTime expiresAt
    ) {
        this.expiresAt = expiresAt;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(
            boolean accepted
    ) {
        this.accepted = accepted;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(
            LocalDateTime acceptedAt
    ) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}