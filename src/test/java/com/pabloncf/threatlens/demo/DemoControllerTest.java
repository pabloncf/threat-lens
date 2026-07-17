package com.pabloncf.threatlens.demo;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Smoke test confirming the demo profile starts cleanly (Flyway migration V3 included) and the
 * endpoints function - not a test of the vulnerability itself, that's the point of the demo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
@Testcontainers
class DemoControllerTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void acceptsCorrectDemoCredentials() throws Exception {
        mockMvc.perform(post("/demo/login").param("username", "alice").param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    @Test
    void rejectsWrongDemoCredentials() throws Exception {
        mockMvc.perform(post("/demo/login").param("username", "alice").param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void storesAndListsAComment() throws Exception {
        mockMvc.perform(post("/demo/comments").param("body", "hello world")).andExpect(status().isOk());

        mockMvc.perform(get("/demo/comments"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hello world")));
    }
}
