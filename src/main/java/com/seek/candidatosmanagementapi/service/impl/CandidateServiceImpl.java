package com.seek.candidatosmanagementapi.service.impl;

import com.seek.candidatosmanagementapi.dto.CandidateResponse;
import com.seek.candidatosmanagementapi.dto.CreateCandidateRequest;
import com.seek.candidatosmanagementapi.dto.MetricsResponse;
import com.seek.candidatosmanagementapi.entity.Candidate;
import com.seek.candidatosmanagementapi.exception.BusinessException;
import com.seek.candidatosmanagementapi.exception.DataIntegrityException;
import com.seek.candidatosmanagementapi.repository.CandidateRepository;
import com.seek.candidatosmanagementapi.service.CandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de candidatos.
 * Maneja la lógica de negocio para crear candidatos, listar y calcular métricas.
 *
 * @author Jose Osorio Catalan
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CandidateServiceImpl implements CandidateService {
    private final CandidateRepository repository;

    /**
     * Crea un nuevo candidato validando datos y calculando edad automáticamente.
     * @param request datos del candidato
     * @return candidato creado con información calculada
     */
    @Override
    public CandidateResponse createCandidate(CreateCandidateRequest request) {
        try {
            log.info("Creating new candidate: {} {}", request.getFirstName(), request.getLastName());

            validateCandidateData(request);

            int calculatedAge = calculateAge(request.getBirthDate());

            if (Math.abs(request.getAge() - calculatedAge) > 1) {
                throw new BusinessException(
                        String.format("La edad proporcionada (%d) no coincide con la fecha de nacimiento. Edad calculada: %d",
                                request.getAge(), calculatedAge)
                );
            }

            Candidate entity = Candidate.builder()
                    .firstName(request.getFirstName().trim())
                    .lastName(request.getLastName().trim())
                    .age(calculatedAge)
                    .birthDate(request.getBirthDate())
                    .build();

            Candidate saved = repository.save(entity);
            log.info("Candidate created successfully with ID: {}", saved.getId());

            return mapToResponse(saved);

        } catch (DataIntegrityViolationException ex) {
            log.error("Data integrity violation while creating candidate", ex);
            throw new DataIntegrityException("No se pudo crear el candidato. Posible duplicación de datos.");
        } catch (BusinessException ex) {
            log.warn("Business validation failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while creating candidate", ex);
            throw new RuntimeException("Error inesperado al crear el candidato", ex);
        }
    }

    /**
     * Obtiene todos los candidatos registrados en el sistema.
     * @return lista de candidatos con datos calculados
     */
    @Override
    @Transactional(readOnly = true)
    public List<CandidateResponse> getAllCandidates() {
        try {
            log.info("Retrieving all candidates");

            List<Candidate> candidates = repository.findAll();

            if (candidates.isEmpty()) {
                log.info("No candidates found in database");
                return List.of();
            }

            List<CandidateResponse> responses = candidates.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            log.info("Retrieved {} candidates successfully", responses.size());
            return responses;

        } catch (Exception ex) {
            log.error("Error retrieving candidates", ex);
            throw new RuntimeException("Error al obtener la lista de candidatos", ex);
        }
    }

    /**
     * Calcula métricas estadísticas de edad de todos los candidatos.
     * @return promedio y desviación estándar de edades
     */
    @Override
    @Transactional(readOnly = true)
    public MetricsResponse getMetrics() {
        try {
            log.info("Calculating candidate metrics");

            List<Candidate> candidates = repository.findAll();

            if (candidates.isEmpty()) {
                log.warn("No candidates found for metrics calculation");
                throw new BusinessException("No hay candidatos registrados para calcular métricas");
            }

            List<Integer> ages = candidates.stream()
                    .map(Candidate::getAge)
                    .collect(Collectors.toList());

            double average = ages.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);

            double stdDeviation = calculateStandardDeviation(ages, average);

            MetricsResponse response = MetricsResponse.builder()
                    .averageAge(average)
                    .ageStdDeviation(stdDeviation)
                    .build();

            log.info("Metrics calculated successfully. Average: {}, StdDev: {}", average, stdDeviation);
            return response;

        } catch (BusinessException ex) {
            log.warn("Business validation failed for metrics: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error calculating metrics", ex);
            throw new RuntimeException("Error al calcular las métricas", ex);
        }
    }

    /**
     * Valida que los datos del candidato cumplan las reglas de negocio.
     */
    private void validateCandidateData(CreateCandidateRequest request) {
        if (request.getBirthDate().isAfter(LocalDate.now())) {
            throw new BusinessException("La fecha de nacimiento no puede ser en el futuro");
        }

        if (request.getAge() < 0 || request.getAge() > 150) {
            throw new BusinessException("La edad debe estar entre 0 y 150 años");
        }

        if (request.getBirthDate().isBefore(LocalDate.now().minusYears(150))) {
            throw new BusinessException("La fecha de nacimiento no puede ser anterior a 150 años");
        }
    }

    /**
     * Calcula la edad actual desde la fecha de nacimiento.
     */
    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Calcula la desviación estándar de una lista de edades.
     */
    private double calculateStandardDeviation(List<Integer> ages, double average) {
        if (ages.size() <= 1) {
            return 0.0;
        }

        double variance = ages.stream()
                .mapToDouble(age -> Math.pow(age - average, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }

    /**
     * Convierte entidad Candidate a DTO CandidateResponse con cálculos adicionales.
     * Calcula próximo cumpleaños, días restantes, edad en meses y fecha estimada.
     */
    private CandidateResponse mapToResponse(Candidate c) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate estimatedEventDate = c.getBirthDate().plusYears(75);

            LocalDate nextBirthday = c.getBirthDate().withYear(today.getYear());
            if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
                nextBirthday = nextBirthday.plusYears(1);
            }

            long daysToNextBirthday = ChronoUnit.DAYS.between(today, nextBirthday);
            long ageInMonths = ChronoUnit.MONTHS.between(c.getBirthDate(), today);

            return CandidateResponse.builder()
                    .id(c.getId())
                    .firstName(c.getFirstName())
                    .lastName(c.getLastName())
                    .age(c.getAge())
                    .birthDate(c.getBirthDate())
                    .estimatedEventDate(estimatedEventDate)
                    .nextBirthday(nextBirthday)
                    .daysToNextBirthday(daysToNextBirthday)
                    .ageInMonths(ageInMonths)
                    .build();

        } catch (Exception ex) {
            log.error("Error mapping candidate to response: {}", ex.getMessage());
            throw new RuntimeException("Error al procesar los datos del candidato", ex);
        }
    }
}