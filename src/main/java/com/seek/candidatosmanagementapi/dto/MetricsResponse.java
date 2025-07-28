package com.seek.candidatosmanagementapi.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsResponse {
    private double averageAge;
    private double ageStdDeviation;
}
