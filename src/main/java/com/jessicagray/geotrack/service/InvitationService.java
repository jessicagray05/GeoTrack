package com.jessicagray.geotrack.service;

import com.jessicagray.geotrack.model.Invitation;
import com.jessicagray.geotrack.model.Organisation;
import com.jessicagray.geotrack.model.User;
import com.jessicagray.geotrack.repository.InvitationRepository;
import com.jessicagray.geotrack.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class InvitationService {

    private static final int TOKEN_BYTES = 32;
    private static final int EXPIRY_HOURS = 48;

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public InvitationService(
            InvitationRepository invitationRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.invitationRepository =
                invitationRepository;

        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;
    }

    /*
     * ---------------------------------------------------------
     * GET CURRENT ORGANISATION INVITATIONS
     * ---------------------------------------------------------
     */

    public List<Invitation> getCurrentOrganisationInvitations() {

        User administrator =
                getCurrentAdministrator();

        return invitationRepository
                .findAllByOrganisationId(
                        administrator
                                .getOrganisation()
                                .getId()
                );
    }

    /*
     * ---------------------------------------------------------
     * CREATE INVITATION
     * ---------------------------------------------------------
     *
     * Returns the RAW token once.
     *
     * The raw token must never be stored in the database.
     * Only its SHA-256 hash is persisted.
     */

    public CreatedInvitation createInvitation(
            String fullName,
            String email
    ) {

        User administrator =
                getCurrentAdministrator();

        Organisation organisation =
                administrator.getOrganisation();

        String cleanedName =
                clean(fullName);

        String cleanedEmail =
                clean(email).toLowerCase();

        if (cleanedName.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter the staff member's name."
            );
        }

        if (!isValidEmail(cleanedEmail)) {
            throw new IllegalArgumentException(
                    "Please enter a valid email address."
            );
        }

        /*
         * A person who already has a GeoTrack account
         * cannot be invited as a brand-new account.
         */
        if (userRepository
                .existsByEmailIgnoreCase(
                        cleanedEmail
                )
                || userRepository
                .existsByUsernameIgnoreCase(
                        cleanedEmail
                )) {

            throw new IllegalArgumentException(
                    "A GeoTrack account already exists with that email address."
            );
        }

        /*
         * Prevent multiple unaccepted invitations for
         * the same email inside this organisation.
         */
        if (invitationRepository
                .existsByEmailIgnoreCaseAndOrganisationIdAndAcceptedFalse(
                        cleanedEmail,
                        organisation.getId()
                )) {

            throw new IllegalArgumentException(
                    "An active invitation already exists for that email address."
            );
        }

        String rawToken =
                generateRawToken();

        String tokenHash =
                hashToken(rawToken);

        Invitation invitation =
                new Invitation();

        /*
         * Organisation is always taken from the
         * authenticated ADMIN.
         *
         * The browser cannot choose an organisation.
         */
        invitation.setOrganisation(
                organisation
        );

        invitation.setFullName(
                cleanedName
        );

        invitation.setEmail(
                cleanedEmail
        );

        invitation.setRole(
                "STAFF"
        );

        invitation.setTokenHash(
                tokenHash
        );

        invitation.setExpiresAt(
                LocalDateTime.now()
                        .plusHours(EXPIRY_HOURS)
        );

        invitation.setAccepted(false);

        invitation =
                invitationRepository.save(
                        invitation
                );

        return new CreatedInvitation(
                invitation,
                rawToken
        );
    }

    /*
     * ---------------------------------------------------------
     * FIND VALID INVITATION
     * ---------------------------------------------------------
     *
     * Used later by the public invitation acceptance page.
     */

    public Optional<Invitation> findValidInvitation(
            String rawToken
    ) {

        if (rawToken == null
                || rawToken.isBlank()) {

            return Optional.empty();
        }

        String tokenHash =
                hashToken(rawToken);

        Optional<Invitation> optionalInvitation =
                invitationRepository
                        .findByTokenHash(
                                tokenHash
                        );

        if (optionalInvitation.isEmpty()) {
            return Optional.empty();
        }

        Invitation invitation =
                optionalInvitation.get();

        if (invitation.isAccepted()) {
            return Optional.empty();
        }

        if (invitation.getExpiresAt() == null
                || invitation
                        .getExpiresAt()
                        .isBefore(
                                LocalDateTime.now()
                        )) {

            return Optional.empty();
        }

        if (invitation.getOrganisation() == null
                || !invitation
                        .getOrganisation()
                        .isEnabled()) {

            return Optional.empty();
        }

        return Optional.of(
                invitation
        );
    }

    /*
     * ---------------------------------------------------------
     * ACCEPT INVITATION
     * ---------------------------------------------------------
     *
     * A valid, unexpired, unused invitation is exchanged
     * for a real GeoTrack STAFF account.
     *
     * The organisation and role come from the stored
     * invitation. They are never accepted from the browser.
     */

    @Transactional
    public User acceptInvitation(
            String rawToken,
            String password
    ) {

        if (rawToken == null
                || rawToken.isBlank()) {

            throw new IllegalArgumentException(
                    "This invitation link is invalid."
            );
        }

        String newPassword =
                password == null
                        ? ""
                        : password;

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        Invitation invitation =
                findValidInvitation(rawToken)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "This invitation is invalid, expired, or has already been used."
                                )
                        );

        String email =
                clean(invitation.getEmail())
                        .toLowerCase();

        if (userRepository
                .existsByEmailIgnoreCase(email)
                || userRepository
                .existsByUsernameIgnoreCase(email)) {

            throw new IllegalArgumentException(
                    "A GeoTrack account already exists with this email address."
            );
        }

        Organisation organisation =
                invitation.getOrganisation();

        if (organisation == null
                || !organisation.isEnabled()) {

            throw new IllegalStateException(
                    "The organisation for this invitation is unavailable."
            );
        }

        User user =
                new User();

        user.setUsername(email);
        user.setEmail(email);
        user.setFullName(
                clean(invitation.getFullName())
        );

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        /*
         * Invitations currently create STAFF only.
         *
         * Do not trust a role supplied by the browser.
         */
        user.setRole("STAFF");
        user.setEnabled(true);
        user.setOrganisation(
                organisation
        );

        user =
                userRepository.save(
                        user
                );

        /*
         * Mark the invitation as consumed only after the
         * account has been created successfully.
         *
         * @Transactional keeps these changes together.
         */
        invitation.setAccepted(true);
        invitation.setAcceptedAt(
                LocalDateTime.now()
        );

        invitationRepository.save(
                invitation
        );

        return user;
    }

    /*
     * ---------------------------------------------------------
     * CURRENT ADMINISTRATOR
     * ---------------------------------------------------------
     */

    private User getCurrentAdministrator() {

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

        User user =
                userRepository
                        .findByEmailIgnoreCase(
                                loginIdentifier
                        )
                        .or(() ->
                                userRepository
                                        .findByUsernameIgnoreCase(
                                                loginIdentifier
                                        )
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Authenticated GeoTrack user could not be found."
                                )
                        );

        if (!user.isEnabled()) {
            throw new IllegalStateException(
                    "GeoTrack user is disabled."
            );
        }

        if (user.getOrganisation() == null) {
            throw new IllegalStateException(
                    "GeoTrack user is not assigned to an organisation."
            );
        }

        if (!user
                .getOrganisation()
                .isEnabled()) {

            throw new IllegalStateException(
                    "GeoTrack organisation is disabled."
            );
        }

        if (!"ADMIN".equalsIgnoreCase(
                user.getRole()
        )) {

            throw new SecurityException(
                    "Only organisation administrators can manage team invitations."
            );
        }

        return user;
    }

    /*
     * ---------------------------------------------------------
     * TOKEN GENERATION
     * ---------------------------------------------------------
     */

    private String generateRawToken() {

        byte[] tokenBytes =
                new byte[TOKEN_BYTES];

        secureRandom.nextBytes(
                tokenBytes
        );

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        tokenBytes
                );
    }

    /*
     * ---------------------------------------------------------
     * TOKEN HASHING
     * ---------------------------------------------------------
     */

    private String hashToken(
            String rawToken
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder hexadecimal =
                    new StringBuilder();

            for (byte value : hash) {

                hexadecimal.append(
                        String.format(
                                "%02x",
                                value
                        )
                );
            }

            return hexadecimal.toString();

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 is not available.",
                    exception
            );
        }
    }

    /*
     * ---------------------------------------------------------
     * STRING CLEANING
     * ---------------------------------------------------------
     */

    private String clean(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    /*
     * ---------------------------------------------------------
     * EMAIL VALIDATION
     * ---------------------------------------------------------
     */

    private boolean isValidEmail(
            String email
    ) {

        return email != null
                && email.matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                );
    }

    /*
     * ---------------------------------------------------------
     * CREATED INVITATION RESULT
     * ---------------------------------------------------------
     *
     * Keeps the persisted invitation and the one-time
     * raw token together without storing the raw token
     * inside the Invitation entity.
     */

    public record CreatedInvitation(
            Invitation invitation,
            String rawToken
    ) {
    }
}