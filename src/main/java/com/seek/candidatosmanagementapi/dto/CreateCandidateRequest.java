package com.seek.candidatosmanagementapi.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO para la creación de un candidato.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCandidateRequest {
    /** Nombre del candidato (no puede estar vacío) */
    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    /** Apellido del candidato (no puede estar vacío) */
    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    /** Edad del candidato (no puede ser nula, mínimo 0) */
    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad debe ser un número positivo")
    private Integer age;

    /** Fecha de nacimiento (no puede ser nula, debe estar en el pasado) */
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate birthDate;
}
