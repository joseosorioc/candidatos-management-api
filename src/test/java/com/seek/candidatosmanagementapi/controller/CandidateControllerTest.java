package com.seek.candidatosmanagementapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seek.candidatosmanagementapi.dto.CandidateResponse;
import com.seek.candidatosmanagementapi.dto.CreateCandidateRequest;
import com.seek.candidatosmanagementapi.dto.MetricsResponse;
import com.seek.candidatosmanagementapi.exception.BusinessException;
import com.seek.candidatosmanagementapi.exception.DataIntegrityException;
import com.seek.candidatosmanagementapi.service.CandidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CandidateController.class)
@DisplayName("CandidateController Tests")
class CandidateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CandidateService candidateService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateCandidateRequest validRequest;
    private CandidateResponse candidateResponse;

    @BeforeEach
    void setUp() {
        LocalDate birthDate = LocalDate.of(1990, 5, 15);
        int calculatedAge = Period.between(birthDate, LocalDate.now()).getYears();

        validRequest = CreateCandidateRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .age(calculatedAge)
                .birthDate(birthDate)
                .build();

        candidateResponse = CandidateResponse.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .age(calculatedAge)
                .birthDate(birthDate)
                .estimatedEventDate(birthDate.plusYears(75))
                .nextBirthday(LocalDate.of(2026, 5, 15))
                .daysToNextBirthday(290L)
                .ageInMonths(calculatedAge * 12L)
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER"})
    @DisplayName("Should create candidate successfully with authentication")
    void shouldCreateCandidateSuccessfully() throws Exception {
        when(candidateService.createCandidate(any(CreateCandidateRequest.class)))
                .thenReturn(candidateResponse);

        mockMvc.perform(post("/api/v1/candidatos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Pérez"))
                .andExpect(jsonPath("$.age").value(35));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"USER"})
    @DisplayName("Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        CreateCandidateRequest invalidRequest = CreateCandidateRequest.builder()
                .firstName("")
                .lastName("Pérez")
                .age(-5)
                .birthDate(LocalDate.now().plusDays(1))
                .build();

        mockMvc.perform(post("/api/v1/candidatos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER"})
    @DisplayName("Should return 409 when business exception occurs")
    void shouldReturn409WhenBusinessException() throws Exception {
        when(candidateService.createCandidate(any(CreateCandidateRequest.class)))
                .thenThrow(new BusinessException("La edad no coincide con la fecha de nacimiento"));

        mockMvc.perform(post("/api/v1/candidatos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("La edad no coincide con la fecha de nacimiento"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER"})
    @DisplayName("Should return 422 when data integrity exception occurs")
    void shouldReturn422WhenDataIntegrityException() throws Exception {
        when(candidateService.createCandidate(any(CreateCandidateRequest.class)))
                .thenThrow(new DataIntegrityException("Violación de integridad de datos"));

        mockMvc.perform(post("/api/v1/candidatos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER"})
    @DisplayName("Should return 500 when unexpected exception occurs")
    void shouldReturn500WhenUnexpectedException() throws Exception {
        when(candidateService.createCandidate(any(CreateCandidateRequest.class)))
                .thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(post("/api/v1/candidatos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER"})
    @DisplayName("Should get all candidates successfully")
    void shouldGetAllCandidatesSuccessfully() throws Exception {
        List<CandidateResponse> candidates = Arrays.asList(candidateResponse);
        when(candidateService.getAllCandidates()).thenReturn(candidates);

        mockMvc.perform(get("/api/v1/candidatos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("Juan"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER"})
    @DisplayName("Should return empty list when no candidates")
    void shouldReturnEmptyListWhenNoCandidates() throws Exception {
        when(candidateService.getAllCandidates()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/candidatos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER"})
    @DisplayName("Should get metrics successfully")
    void shouldGetMetricsSuccessfully() throws Exception {
        MetricsResponse metrics = MetricsResponse.builder()
                .averageAge(30.0)
                .ageStdDeviation(5.2)
                .build();
        when(candidateService.getMetrics()).thenReturn(metrics);

        mockMvc.perform(get("/api/v1/candidatos/metrics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.averageAge").value(30.0))
                .andExpect(jsonPath("$.ageStdDeviation").value(5.2));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER"})
    @DisplayName("Should return 409 when no candidates for metrics")
    void shouldReturn409WhenNoCandidatesForMetrics() throws Exception {
        when(candidateService.getMetrics())
                .thenThrow(new BusinessException("No hay candidatos registrados para calcular métricas"));

        mockMvc.perform(get("/api/v1/candidatos/metrics"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("No hay candidatos registrados para calcular métricas"));
    }
}