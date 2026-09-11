package com.jessicagray.geotrack;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTests {

    @Autowired
    private MockMvc mockMvc;

    /*
     * ADMIN users with a valid CSRF token are permitted
     * through Spring Security to reach the project
     * deletion endpoint.
     *
     * Project 999999 deliberately does not exist, so once
     * the request passes security the controller returns 404.
     *
     * A 403 response here would mean the request was blocked
     * by Spring Security or CSRF protection.
     */
    @Test
    void adminCanAccessDeleteProjectEndpoint()
            throws Exception {

        mockMvc.perform(
                delete("/api/projects/999999")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .with(
                                user("admin")
                                        .roles("ADMIN")
                        )
                        .with(
                                csrf()
                        )
        )
        .andExpect(
                status().isNotFound()
        );
    }

    /*
     * STAFF users must never be allowed to delete projects.
     *
     * A valid CSRF token is deliberately supplied here so
     * that the 403 response proves the role restriction is
     * blocking STAFF, rather than CSRF protection.
     */
    @Test
    void staffCannotDeleteProject()
            throws Exception {

        mockMvc.perform(
                delete("/api/projects/999999")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .with(
                                user("staff")
                                        .roles("STAFF")
                        )
                        .with(
                                csrf()
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    /*
     * Even an authenticated ADMIN must not be allowed to
     * perform a state-changing request without a valid
     * CSRF token.
     */
    @Test
    void adminCannotDeleteProjectWithoutCsrf()
            throws Exception {

        mockMvc.perform(
                delete("/api/projects/999999")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .with(
                                user("admin")
                                        .roles("ADMIN")
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    /*
     * An unauthenticated user with a valid CSRF token must
     * still be stopped by authentication and redirected to
     * GeoTrack's login page.
     *
     * Supplying CSRF here isolates authentication behaviour
     * from CSRF behaviour.
     */
    @Test
    void unauthenticatedUserCannotDeleteProject()
            throws Exception {

        mockMvc.perform(
                delete("/api/projects/999999")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .with(
                                csrf()
                        )
        )
        .andExpect(
                status().is3xxRedirection()
        );
    }

    /*
     * STAFF users are allowed to view projects.
     *
     * GET requests do not require CSRF tokens.
     * ProjectService then restricts the returned projects
     * to the user's organisation.
     */
    @Test
    void staffCanViewProjects()
            throws Exception {

        mockMvc.perform(
                get("/api/projects")
                        .with(
                                user("staff")
                                        .roles("STAFF")
                        )
        )
        .andExpect(
                status().isOk()
        );
    }

    /*
     * ADMIN users are also allowed to view projects.
     *
     * Tenant filtering still applies even to ADMIN users.
     */
    @Test
    void adminCanViewProjects()
            throws Exception {

        mockMvc.perform(
                get("/api/projects")
                        .with(
                                user("admin")
                                        .roles("ADMIN")
                        )
        )
        .andExpect(
                status().isOk()
        );
    }
}