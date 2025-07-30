package com.seek.candidatosmanagementapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entidad que representa un candidato y mapea la tabla "candidates" en la base de datos.
 */
@Entity
@Table(name = "candidates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    /**
     * Identificador único del candidato.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del candidato.
     */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * Apellido del candidato.
     */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * Edad del candidato.
     */
    @Column(nullable = false)
    private Integer age;

    /**
     * Fecha de nacimiento del candidato.
     */
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
}