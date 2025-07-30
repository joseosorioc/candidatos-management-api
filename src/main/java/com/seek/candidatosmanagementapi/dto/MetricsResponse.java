package com.seek.candidatosmanagementapi.dto;

import lombok.*;

/**
 * DTO para métricas de edad de los candidatos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsResponse {
    /** Edad promedio de los candidatos */
    private double averageAge;

    /** Desviación estándar de las edades */
    private double ageStdDeviation;
}
