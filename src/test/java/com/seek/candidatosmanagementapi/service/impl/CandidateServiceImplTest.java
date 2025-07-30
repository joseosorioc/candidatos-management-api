package com.seek.candidatosmanagementapi.service.impl;


import com.seek.candidatosmanagementapi.dto.CandidateResponse;
import com.seek.candidatosmanagementapi.dto.CreateCandidateRequest;
import com.seek.candidatosmanagementapi.dto.MetricsResponse;
import com.seek.candidatosmanagementapi.entity.Candidate;
import com.seek.candidatosmanagementapi.exception.BusinessException;
import com.seek.candidatosmanagementapi.exception.DataIntegrityException;
import com.seek.candidatosmanagementapi.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CandidateService Tests")
class CandidateServiceImplTest {

    @Mock
    private CandidateRepository repository;

    @InjectMocks
    private CandidateServiceImpl candidateService;

    private CreateCandidateRequest validRequest;
    private Candidate candidateEntity;

    @BeforeEach
    void setUp() {
        LocalDate birthDate = LocalDate.of(1990, 5, 15);

        validRequest = CreateCandidateRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .age(35)
                .birthDate(birthDate)
                .build();

        candidateEntity = Candidate.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .age(33)
                .birthDate(birthDate)
                .build();
    }

    @Test
    @DisplayName("Should create candidate successfully")
    void shouldCreateCandidateSuccessfully() {
        when(repository.save(any(Candidate.class))).thenReturn(candidateEntity);

        CandidateResponse response = candidateService.createCandidate(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("Juan");
        assertThat(response.getLastName()).isEqualTo("Pérez");
        assertThat(response.getAge()).isEqualTo(33);
        assertThat(response.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(response.getEstimatedEventDate()).isEqualTo(LocalDate.of(2065, 5, 15));

        verify(repository, times(1)).save(any(Candidate.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when birth date is in future")
    void shouldThrowBusinessExceptionWhenBirthDateInFuture() {
        CreateCandidateRequest invalidRequest = CreateCandidateRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .age(25)
                .birthDate(LocalDate.now().plusDays(1))
                .build();

        assertThatThrownBy(() -> candidateService.createCandidate(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("La fecha de nacimiento no puede ser en el futuro");

        verify(repository, never()).save(any(Candidate.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when age doesn't match birth date")
    void shouldThrowBusinessExceptionWhenAgeDoesntMatchBirthDate() {
        CreateCandidateRequest invalidRequest = CreateCandidateRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .age(50)
                .birthDate(LocalDate.of(1990, 5, 15))
                .build();

        assertThatThrownBy(() -> candidateService.createCandidate(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("La edad proporcionada")
                .hasMessageContaining("no coincide con la fecha de nacimiento");

        verify(repository, never()).save(any(Candidate.class));
    }

    @Test
    @DisplayName("Should throw DataIntegrityException when database constraint violation")
    void shouldThrowDataIntegrityExceptionWhenDatabaseError() {
        when(repository.save(any(Candidate.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThatThrownBy(() -> candidateService.createCandidate(validRequest))
                .isInstanceOf(DataIntegrityException.class)
                .hasMessage("No se pudo crear el candidato. Posible duplicación de datos.");

        verify(repository, times(1)).save(any(Candidate.class));
    }

    @Test
    @DisplayName("Should return all candidates successfully")
    void shouldReturnAllCandidatesSuccessfully() {
        List<Candidate> candidates = Arrays.asList(
                candidateEntity,
                Candidate.builder()
                        .id(2L)
                        .firstName("María")
                        .lastName("García")
                        .age(28)
                        .birthDate(LocalDate.of(1995, 3, 20))
                        .build()
        );
        when(repository.findAll()).thenReturn(candidates);

        List<CandidateResponse> responses = candidateService.getAllCandidates();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getFirstName()).isEqualTo("Juan");
        assertThat(responses.get(1).getFirstName()).isEqualTo("María");

        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no candidates found")
    void shouldReturnEmptyListWhenNoCandidatesFound() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<CandidateResponse> responses = candidateService.getAllCandidates();

        assertThat(responses).isEmpty();
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should calculate metrics successfully")
    void shouldCalculateMetricsSuccessfully() {
        List<Candidate> candidates = Arrays.asList(
                Candidate.builder().age(25).build(),
                Candidate.builder().age(30).build(),
                Candidate.builder().age(35).build()
        );
        when(repository.findAll()).thenReturn(candidates);

        MetricsResponse metrics = candidateService.getMetrics();

        assertThat(metrics.getAverageAge()).isEqualTo(30.0);
        assertThat(metrics.getAgeStdDeviation()).isCloseTo(4.08, within(0.1));

        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should throw BusinessException when no candidates for metrics")
    void shouldThrowBusinessExceptionWhenNoCandidatesForMetrics() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> candidateService.getMetrics())
                .isInstanceOf(BusinessException.class)
                .hasMessage("No hay candidatos registrados para calcular métricas");

        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should calculate standard deviation correctly for single candidate")
    void shouldCalculateStdDeviationForSingleCandidate() {
        List<Candidate> candidates = Arrays.asList(
                Candidate.builder().age(25).build()
        );
        when(repository.findAll()).thenReturn(candidates);

        MetricsResponse metrics = candidateService.getMetrics();

        assertThat(metrics.getAverageAge()).isEqualTo(25.0);
        assertThat(metrics.getAgeStdDeviation()).isEqualTo(0.0);

        verify(repository, times(1)).findAll();
    }
}