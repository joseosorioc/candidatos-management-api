package com.seek.candidatosmanagementapi.dto;

import lombok.*;
import java.time.LocalDate;

/**
 * DTO para devolver información de un candidato.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    /** ID del candidato */
    private Long id;
    /** Nombre */
    private String firstName;
    /** Apellido */
    private String lastName;
    /** Edad en años */
    private Integer age;
    /** Fecha de nacimiento */
    private LocalDate birthDate;
    /** Fecha estimada de evento */
    private LocalDate estimatedEventDate;
    /** Próximo cumpleaños */
    private LocalDate nextBirthday;
    /** Días hasta el próximo cumpleaños */
    private Long daysToNextBirthday;
    /** Edad en meses */
    private Long ageInMonths;
}
