package com.jessicagray.geotrack;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTests {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void adminCanDeleteProject() throws Exception {

        mockMvc.perform(
                delete("/api/projects/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user("admin").roles("ADMIN"))
        )
       .andExpect(
        status().isOk()
        );
    }


    @Test
    void staffCannotDeleteProject() throws Exception {

        mockMvc.perform(
                delete("/api/projects/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user("staff").roles("STAFF"))
        )
        .andExpect(
                status().isForbidden()
        );
    }


    @Test
    void unauthenticatedUserCannotDeleteProject()
            throws Exception {

        mockMvc.perform(
                delete("/api/projects/999999")
                        .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(
                status().is3xxRedirection()
        );
    }


    @Test
    void staffCanViewProjects() throws Exception {

        mockMvc.perform(
                get("/api/projects")
                        .with(user("staff").roles("STAFF"))
        )
        .andExpect(
                status().isOk()
        );
    }


    @Test
    void adminCanViewProjects() throws Exception {

        mockMvc.perform(
                get("/api/projects")
                        .with(user("admin").roles("ADMIN"))
        )
        .andExpect(
                status().isOk()
        );
    }
}