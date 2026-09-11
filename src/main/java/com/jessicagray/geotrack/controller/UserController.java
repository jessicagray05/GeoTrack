package com.jessicagray.geotrack.controller;

import com.jessicagray.geotrack.model.Invitation;
import com.jessicagray.geotrack.model.Organisation;
import com.jessicagray.geotrack.model.User;
import com.jessicagray.geotrack.repository.OrganisationRepository;
import com.jessicagray.geotrack.repository.UserRepository;
import com.jessicagray.geotrack.service.InvitationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;
    private final PasswordEncoder passwordEncoder;
    private final InvitationService invitationService;

    public UserController(
            UserRepository userRepository,
            OrganisationRepository organisationRepository,
            PasswordEncoder passwordEncoder,
            InvitationService invitationService
    ) {
        this.userRepository = userRepository;
        this.organisationRepository = organisationRepository;
        this.passwordEncoder = passwordEncoder;
        this.invitationService = invitationService;
    }

    /*
     * ---------------------------------------------------------
     * CURRENT USER ROLE
     * ---------------------------------------------------------
     */

    @GetMapping("/api/user-role")
    public String getUserRole(
            Authentication authentication
    ) {
        return authentication
                .getAuthorities()
                .stream()
                .findFirst()
                .map(authority ->
                        authority
                                .getAuthority()
                                .replace("ROLE_", "")
                )
                .orElse("STAFF");
    }

    /*
     * ---------------------------------------------------------
     * PUBLIC COMPANY REGISTRATION
     * ---------------------------------------------------------
     *
     * Public registration creates:
     *
     * 1. A brand-new organisation.
     * 2. The organisation's first user.
     * 3. The first user becomes ADMIN.
     *
     * The user is attached to the organisation on the server.
     *
     * The browser is never allowed to supply an organisation ID.
     */

    @PostMapping("/api/auth/register")
    @Transactional
    public ResponseEntity<Map<String, String>> register(
            @RequestBody RegistrationRequest request
    ) {
        Map<String, String> response =
                new HashMap<>();

        String fullName =
                clean(request.fullName());

        String email =
                clean(request.email())
                        .toLowerCase();

        String organisationName =
                clean(request.organisationName());

        String password =
                request.password() == null
                        ? ""
                        : request.password();

        /*
         * Validate name.
         */

        if (fullName.isBlank()) {
            response.put(
                    "message",
                    "Please enter your name."
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }

        /*
         * Validate email.
         */

        if (!isValidEmail(email)) {
            response.put(
                    "message",
                    "Please enter a valid email address."
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }

        /*
         * Every new public account must create a company.
         */

        if (organisationName.isBlank()) {
            response.put(
                    "message",
                    "Please enter your company name."
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }

        if (organisationName.length() < 2) {
            response.put(
                    "message",
                    "Company name must contain at least 2 characters."
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }

        if (organisationName.length() > 150) {
            response.put(
                    "message",
                    "Company name must contain no more than 150 characters."
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }

        /*
         * Password validation.
         */

        if (password.length() < 8) {
            response.put(
                    "message",
                    "Password must contain at least 8 characters."
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }

        /*
         * Email addresses must be unique across GeoTrack.
         */

        if (userRepository
                .existsByEmailIgnoreCase(email)
                || userRepository
                .existsByUsernameIgnoreCase(email)) {

            response.put(
                    "message",
                    "An account already exists with that email address."
            );

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(response);
        }

        /*
         * Organisation names are currently unique.
         *
         * Employees join an existing organisation through
         * the secure Team Management invitation flow.
         */

        if (organisationRepository
                .existsByNameIgnoreCase(
                        organisationName
                )) {

            response.put(
                    "message",
                    "A GeoTrack organisation already exists with that company name."
            );

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(response);
        }

        /*
         * ---------------------------------------------------------
         * CREATE ORGANISATION
         * ---------------------------------------------------------
         */

        Organisation organisation =
                new Organisation(
                        organisationName
                );

        organisation =
                organisationRepository.save(
                        organisation
                );

        /*
         * ---------------------------------------------------------
         * CREATE FIRST ORGANISATION USER
         * ---------------------------------------------------------
         */

        User user = new User();

        /*
         * New accounts use their email address as the internal
         * username so the existing Spring Security login system
         * continues to work.
         */

        user.setUsername(email);
        user.setEmail(email);
        user.setFullName(fullName);

        /*
         * Never store the plain-text password.
         */

        user.setPassword(
                passwordEncoder.encode(
                        password
                )
        );

        /*
         * The person creating the company becomes that
         * organisation's first ADMIN.
         */

        user.setRole("ADMIN");
        user.setEnabled(true);

        /*
         * The organisation is assigned SERVER-SIDE.
         *
         * Registration does not accept an organisation ID from
         * the browser.
         */

        user.setOrganisation(
                organisation
        );

        userRepository.save(user);

        response.put(
                "message",
                "Your company and GeoTrack account have been created."
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * ---------------------------------------------------------
     * CURRENT ACCOUNT
     * ---------------------------------------------------------
     *
     * Returns safe account information only.
     *
     * Password information is never returned.
     */

    @GetMapping("/api/account/me")
    public ResponseEntity<?> getCurrentAccount(
            Authentication authentication
    ) {
        Optional<User> optionalUser =
                findAuthenticatedUser(
                        authentication
                );

        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Account could not be found."
                            )
                    );
        }

        User user =
                optionalUser.get();

        Map<String, Object> account =
                new HashMap<>();

        account.put(
                "id",
                user.getId()
        );

        account.put(
                "fullName",
                user.getFullName() == null
                        ? ""
                        : user.getFullName()
        );

        account.put(
                "email",
                user.getEmail() == null
                        ? user.getUsername()
                        : user.getEmail()
        );

        account.put(
                "role",
                user.getRole()
        );

        account.put(
                "enabled",
                user.isEnabled()
        );

        if (user.getOrganisation() != null) {
            account.put(
                    "organisationId",
                    user
                            .getOrganisation()
                            .getId()
            );

            account.put(
                    "organisationName",
                    user
                            .getOrganisation()
                            .getName()
            );
        } else {
            account.put(
                    "organisationId",
                    null
            );

            account.put(
                    "organisationName",
                    ""
            );
        }

        return ResponseEntity.ok(
                account
        );
    }

    /*
     * ---------------------------------------------------------
     * CHANGE PASSWORD
     * ---------------------------------------------------------
     */

    @PostMapping("/api/account/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request
    ) {
        Map<String, String> response =
                new HashMap<>();

        Optional<User> optionalUser =
                findAuthenticatedUser(
                        authentication
                );

        if (optionalUser.isEmpty()) {
            response.put(
                    "message",
                    "Account could not be found."
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        User user =
                optionalUser.get();

        String currentPassword =
                request.currentPassword() == null
                        ? ""
                        : request.currentPassword();

        String newPassword =
                request.newPassword() == null
                        ? ""
                        : request.newPassword();

        /*
         * Current password must be correct.
         */

        if (!passwordEncoder.matches(
                currentPassword,
                user.getPassword()
        )) {
            response.put(
                    "message",
                    "Your current password is incorrect."
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }

        /*
         * New password must be at least 8 characters.
         */

        if (newPassword.length() < 8) {
            response.put(
                    "message",
                    "New password must contain at least 8 characters."
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }

        /*
         * Prevent reusing the current password.
         */

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword()
        )) {
            response.put(
                    "message",
                    "Your new password must be different from your current password."
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }

        /*
         * Store only the encoded new password.
         */

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        userRepository.save(
                user
        );

        response.put(
                "message",
                "Your password has been changed successfully."
        );

        return ResponseEntity.ok(
                response
        );
    }

    /*
     * ---------------------------------------------------------
     * TEAM MANAGEMENT
     * ---------------------------------------------------------
     *
     * Team data is always derived from the authenticated
     * administrator's organisation.
     *
     * No organisation ID is accepted from the browser.
     */

    @GetMapping("/api/team")
    public ResponseEntity<?> getTeam(
            Authentication authentication
    ) {
        Optional<User> optionalAdministrator =
                findAuthenticatedUser(
                        authentication
                );

        if (optionalAdministrator.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Account could not be found."
                            )
                    );
        }

        User administrator =
                optionalAdministrator.get();

        if (!isOrganisationAdministrator(
                administrator
        )) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "message",
                                    "Only organisation administrators can manage the team."
                            )
                    );
        }

        Organisation organisation =
                administrator.getOrganisation();

        List<User> users =
                userRepository
                        .findAllByOrganisationId(
                                organisation.getId()
                        );

        List<Map<String, Object>> team =
                new ArrayList<>();

        for (User user : users) {
            team.add(
                    safeTeamMember(
                            user
                    )
            );
        }

        return ResponseEntity.ok(
                team
        );
    }

    /*
     * ---------------------------------------------------------
     * LIST TEAM INVITATIONS
     * ---------------------------------------------------------
     */

    @GetMapping("/api/team/invitations")
    public ResponseEntity<?> getTeamInvitations() {
        try {
            List<Invitation> invitations =
                    invitationService
                            .getCurrentOrganisationInvitations();

            List<Map<String, Object>> result =
                    new ArrayList<>();

            for (Invitation invitation : invitations) {
                result.add(
                        safeInvitation(
                                invitation
                        )
                );
            }

            return ResponseEntity.ok(
                    result
            );

        } catch (SecurityException exception) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "message",
                                    exception.getMessage()
                            )
                    );

        } catch (IllegalStateException exception) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    exception.getMessage()
                            )
                    );
        }
    }

    /*
     * ---------------------------------------------------------
     * INVITE STAFF MEMBER
     * ---------------------------------------------------------
     *
     * For the local-development phase this endpoint returns
     * an invitation URL so the complete flow can be tested
     * without an email provider.
     *
     * Before production, the raw token will be sent by email
     * and should not be returned to an authenticated browser.
     */

    @PostMapping("/api/team/invitations")
    public ResponseEntity<?> createTeamInvitation(
            @RequestBody TeamInvitationRequest request
    ) {
        try {
            InvitationService.CreatedInvitation created =
                    invitationService
                            .createInvitation(
                                    request.fullName(),
                                    request.email()
                            );

            Invitation invitation =
                    created.invitation();

            Map<String, Object> response =
                    safeInvitation(
                            invitation
                    );

            response.put(
                    "message",
                    "Invitation created successfully."
            );

            /*
             * LOCAL DEVELOPMENT ONLY.
             *
             * This allows us to test invitation acceptance before
             * SMTP/email delivery is connected.
             */
            response.put(
                    "invitationUrl",
                    "/accept-invite.html?token="
                            + created.rawToken()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    exception.getMessage()
                            )
                    );

        } catch (SecurityException exception) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "message",
                                    exception.getMessage()
                            )
                    );

        } catch (IllegalStateException exception) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    exception.getMessage()
                            )
                    );
        }
    }

    /*
     * ---------------------------------------------------------
     * PUBLIC INVITATION VALIDATION
     * ---------------------------------------------------------
     */

    @PostMapping("/api/auth/invitations/validate")
    public ResponseEntity<?> validateInvitation(
            @RequestBody InvitationTokenRequest request
    ) {
        try {
            Invitation invitation =
                    invitationService
                            .findValidInvitation(
                                    request.token()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "This invitation is invalid, expired, or has already been used."
                                    )
                            );

            Organisation organisation = invitation.getOrganisation();

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("fullName", invitation.getFullName());
            response.put("email", invitation.getEmail());
            response.put(
                    "organisationName",
                    organisation == null ? "" : organisation.getName()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException | IllegalStateException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "This invitation is invalid, expired, or has already been used."
                            )
                    );
        }
    }

    /*
     * ---------------------------------------------------------
     * PUBLIC INVITATION ACCEPTANCE
     * ---------------------------------------------------------
     */

    @PostMapping("/api/auth/invitations/accept")
    public ResponseEntity<?> acceptInvitation(
            @RequestBody AcceptInvitationRequest request
    ) {
        try {
            User user =
                    invitationService.acceptInvitation(
                            request.token(),
                            request.password()
                    );

            String email =
                    user.getEmail() == null
                            ? user.getUsername()
                            : user.getEmail();

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Your GeoTrack account has been created successfully.",
                            "email",
                            email
                    )
            );

        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", exception.getMessage()));

        } catch (IllegalStateException exception) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", exception.getMessage()));
        }
    }

    /*
     * ---------------------------------------------------------
     * SAFE TEAM MEMBER RESPONSE
     * ---------------------------------------------------------
     *
     * Passwords and organisation internals are deliberately
     * excluded.
     */

    private Map<String, Object> safeTeamMember(
            User user
    ) {
        Map<String, Object> result =
                new HashMap<>();

        result.put(
                "id",
                user.getId()
        );

        result.put(
                "fullName",
                user.getFullName() == null
                        ? ""
                        : user.getFullName()
        );

        result.put(
                "email",
                user.getEmail() == null
                        ? user.getUsername()
                        : user.getEmail()
        );

        result.put(
                "role",
                user.getRole()
        );

        result.put(
                "enabled",
                user.isEnabled()
        );

        return result;
    }

    /*
     * ---------------------------------------------------------
     * SAFE INVITATION RESPONSE
     * ---------------------------------------------------------
     *
     * tokenHash is NEVER returned.
     */

    private Map<String, Object> safeInvitation(
            Invitation invitation
    ) {
        Map<String, Object> result =
                new HashMap<>();

        result.put(
                "id",
                invitation.getId()
        );

        result.put(
                "fullName",
                invitation.getFullName()
        );

        result.put(
                "email",
                invitation.getEmail()
        );

        result.put(
                "role",
                invitation.getRole()
        );

        result.put(
                "accepted",
                invitation.isAccepted()
        );

        result.put(
                "createdAt",
                invitation.getCreatedAt()
        );

        result.put(
                "expiresAt",
                invitation.getExpiresAt()
        );

        result.put(
                "acceptedAt",
                invitation.getAcceptedAt()
        );

        return result;
    }

    /*
     * ---------------------------------------------------------
     * ORGANISATION ADMIN CHECK
     * ---------------------------------------------------------
     */

    private boolean isOrganisationAdministrator(
            User user
    ) {
        return user != null
                && user.isEnabled()
                && user.getOrganisation() != null
                && user
                        .getOrganisation()
                        .isEnabled()
                && "ADMIN".equalsIgnoreCase(
                        user.getRole()
                );
    }

    /*
     * ---------------------------------------------------------
     * AUTHENTICATED USER LOOKUP
     * ---------------------------------------------------------
     */

    private Optional<User> findAuthenticatedUser(
            Authentication authentication
    ) {
        if (authentication == null) {
            return Optional.empty();
        }

        String username =
                authentication.getName();

        return userRepository
                .findByUsernameIgnoreCase(
                        username
                )
                .or(() ->
                        userRepository
                                .findByEmailIgnoreCase(
                                        username
                                )
                );
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
     * BASIC EMAIL VALIDATION
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
     * REGISTRATION REQUEST
     * ---------------------------------------------------------
     */

    public record RegistrationRequest(
            String fullName,
            String email,
            String organisationName,
            String password
    ) {
    }

    /*
     * ---------------------------------------------------------
     * CHANGE PASSWORD REQUEST
     * ---------------------------------------------------------
     */

    public record ChangePasswordRequest(
            String currentPassword,
            String newPassword
    ) {
    }

    /*
     * ---------------------------------------------------------
     * PUBLIC INVITATION TOKEN REQUEST
     * ---------------------------------------------------------
     */

    public record InvitationTokenRequest(
            String token
    ) {
    }

    /*
     * ---------------------------------------------------------
     * PUBLIC INVITATION ACCEPTANCE REQUEST
     * ---------------------------------------------------------
     */

    public record AcceptInvitationRequest(
            String token,
            String password
    ) {
    }

    /*
     * ---------------------------------------------------------
     * TEAM INVITATION REQUEST
     * ---------------------------------------------------------
     */

    public record TeamInvitationRequest(
            String fullName,
            String email
    ) {
    }
}
