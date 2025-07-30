package com.seek.candidatosmanagementapi.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${security.user.name}")
    private String securityUsername;

    @Value("${security.user.password}")
    private String securityPassword;

    @Test
    @DisplayName("Swagger UI without authentication should return 401")
    void swaggerUiWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/swagger-ui.html"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("API docs without authentication should return 401")
    void apiDocsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v3/api-docs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Actuator health without authentication should return 401")
    void actuatorHealthWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/actuator/health"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Actuator metrics without authentication should return 401")
    void actuatorMetricsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/actuator/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoints require authentication")
    void protectedEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/v1/candidatos"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", "application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @DisplayName("Valid credentials allow successful request")
    void validCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/candidatos")
                        .with(httpBasic(securityUsername, securityPassword)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Invalid credentials return 401")
    void invalidCredentials() throws Exception {
        // username incorrecto
        mockMvc.perform(get("/api/v1/candidatos")
                        .with(httpBasic("unknown", securityPassword)))
                .andExpect(status().isUnauthorized());
        // le coloco una pass malaa
        mockMvc.perform(get("/api/v1/candidatos")
                        .with(httpBasic(securityUsername, "wrongpass")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Empty credentials return 401")
    void emptyCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/candidatos")
                        .with(httpBasic("", "")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST without authentication returns 401")
    void postWithoutAuth() throws Exception {
        String json = """
            {
              "firstName":"Test",
              "lastName":"User",
              "age":25,
              "birthDate":"1999-01-01"
            }
            """;
        mockMvc.perform(post("/api/v1/candidatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST with authentication creates candidate successfully")
    void postWithAuth() throws Exception {
        String json = """
        {
          "firstName":"Test",
          "lastName":"User",
          "age":25,
          "birthDate":"1999-01-01"
        }
        """;
        mockMvc.perform(post("/api/v1/candidatos")
                        .with(httpBasic(securityUsername, securityPassword))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    @DisplayName("Sucess when session is stateless (no session maintained)")
    void statelessSession() throws Exception {
        mockMvc.perform(get("/api/v1/candidatos")
                        .with(httpBasic(securityUsername, securityPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/v1/candidatos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Errors include Content-Type header with charset")
    void errorContentType() throws Exception {
        mockMvc.perform(get("/api/v1/candidatos"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", "application/json;charset=UTF-8"));
    }

    @Test
    @DisplayName("Error responses include a timestamp property")
    void errorContainsTimestamp() throws Exception {
        mockMvc.perform(get("/api/v1/candidatos"))
                 .andExpect(status().isUnauthorized())
                 .andExpect(jsonPath("$.timestamp").isString());
    }
}