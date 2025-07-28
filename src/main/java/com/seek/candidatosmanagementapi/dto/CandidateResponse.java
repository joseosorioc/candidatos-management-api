package com.seek.candidatosmanagementapi.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private Integer age;
    private LocalDate birthDate;
    private LocalDate estimatedEventDate;
    private LocalDate nextBirthday;
    private Long daysToNextBirthday;
    private Long ageInMonths;
}